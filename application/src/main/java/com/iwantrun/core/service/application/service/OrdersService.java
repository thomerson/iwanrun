package com.iwantrun.core.service.application.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.iwantrun.core.constant.AdminApplicationConstants;
import com.iwantrun.core.service.application.dao.JPQLEnableRepository;
import com.iwantrun.core.service.application.dao.OrderAttachmentsDao;
import com.iwantrun.core.service.application.dao.OrderMessageDao;
import com.iwantrun.core.service.application.dao.OrdersDao;
import com.iwantrun.core.service.application.dao.PurchaserAccountDao;
import com.iwantrun.core.service.application.dao.UserInfoDao;
import com.iwantrun.core.service.application.domain.OrderAttachments;
import com.iwantrun.core.service.application.domain.OrderMessage;
import com.iwantrun.core.service.application.domain.Orders;
import com.iwantrun.core.service.application.domain.PurchaserAccount;
import com.iwantrun.core.service.application.domain.UserInfo;
import com.iwantrun.core.service.application.enums.TradeStatus;
import com.iwantrun.core.service.application.transfer.SimpleMessageBody;
import com.iwantrun.core.service.utils.EntityBeanUtils;
import com.iwantrun.core.service.utils.JSONUtils;
import com.iwantrun.core.service.utils.OrderNoGenerator;
import com.iwantrun.core.service.utils.PageDataWrapUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@Service
public class OrdersService {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private JPQLEnableRepository jpql;
	
	@Autowired
	private OrdersDao ordersDao ;
	
	@Autowired
	private PurchaserAccountDao purchaseDao;
	
	@Autowired
	private OrderAttachmentsDao ordersAttacgDao;
	
	@Autowired
	private OrderMessageDao orderMessageDao;
	
	@Autowired
	private UserInfoDao infoDao ;
	
	@Autowired  
    private Environment env;
	@Autowired
	private JPQLEnableRepository jpqlExecute;
	
	Logger logger = LoggerFactory.getLogger(OrdersService.class);
	
	public String findAll(JSONObject requestObj) {
		String pageIndexStr = requestObj.getAsString("pageIndex");
		Integer pageIndex = pageIndexStr == null ? 0:Integer.parseInt(pageIndexStr);
		Integer pageSize = Integer.parseInt(environment.getProperty("common.pageSize"));
		List<Map<String,Object>> resultList = ordersDao.findAllWithPurchaseInfoPaged(pageIndex, pageSize, jpql);
		Integer total = ordersDao.countAllWithPurchaseInfo(jpql);
		Pageable page =  PageRequest.of(pageIndex, pageSize, Sort.Direction.ASC, "id");
		PageImpl<Map<String,Object>> result = new PageImpl<Map<String,Object>>(resultList, page, total);
		return PageDataWrapUtils.page2JsonNoCopy(result);
	}
	
	public String findByExample(JSONObject requestObj) {
		String pageIndexStr = requestObj.getAsString("pageIndex");
		int pageSize = Integer.parseInt(env.getProperty("common.pageSize"));
		String objString = requestObj.getAsString("obj");
		JSONObject obj = (JSONObject) JSONValue.parse(objString);
		Integer pageIndex = pageIndexStr == null ? 0:Integer.parseInt(pageIndexStr);
		obj.put("pageIndex", pageIndex);
		obj.put("pageSize", pageSize);
		String orderStatusStr = obj.getAsString("orderStatus");
		Integer orderStatus = orderStatusStr == null ? null :Integer.parseInt(orderStatusStr);
		obj.put("orderStatus", orderStatus);
		List<Map<String,Object>> resultList = ordersDao.findByExampleWithUserInfoPaged(obj, jpql);
		Integer total = ordersDao.countByExampleWithUserInfo(obj, jpql);
		Pageable page =  PageRequest.of(pageIndex, pageSize, Sort.Direction.ASC, "id");
		PageImpl<Map<String,Object>> result = new PageImpl<Map<String,Object>>(resultList, page, total);
		return PageDataWrapUtils.page2JsonNoCopy(result);
	}

	public Map<String,Object> get(JSONObject requestObj) {
		Number idNum = requestObj.getAsNumber("id");
		Map<String,Object> resultMap = new HashMap<String,Object>();
		if(idNum != null) {
			Integer id = idNum.intValue();
			Optional<Orders> ordersOp = ordersDao.findById(id);
			if(ordersOp.isPresent()) {
				//fetch order
				Orders order = ordersOp.get();
				resultMap.put("orders", order);
				//fetch purchaseUser 
				Integer ownerId = order.getOrderOwnerId();
				if(ownerId != null) {
					Optional<PurchaserAccount> account = purchaseDao.findById(ownerId);
					resultMap.put("purchaserAccount", account.get());
					List<UserInfo> pirchaseInfoList = infoDao.findByLoginInfoId(ownerId);
					if(pirchaseInfoList != null && pirchaseInfoList.size() > 0) {
						resultMap.put("purchaserAccountInfo", pirchaseInfoList.get(0));
					}
				}
				//fetch adviser 
				Integer orderAdviserId = order.getOrderAdviserId();
				if(orderAdviserId != null) {
					List<UserInfo> adviser = infoDao.findByLoginInfoId(orderAdviserId);
					resultMap.put("adviserAccount", adviser.get(0));
				}
				//fetch orders attachment 
				List<OrderAttachments> caseDraft = ordersAttacgDao.findByOrderIdAndPagePath(id, AdminApplicationConstants.CASE_DRAFT);
				if(caseDraft != null && caseDraft.size() > 0) {
					resultMap.put("caseDraft", caseDraft);
				}
				List<OrderAttachments> appointment = ordersAttacgDao.findByOrderIdAndPagePath(id, AdminApplicationConstants.APPOINTMENT);
				if(appointment != null && appointment.size() > 0) {
					resultMap.put("appointment", appointment);
				}
				List<OrderAttachments> projectConclude = ordersAttacgDao.findByOrderIdAndPagePath(id, AdminApplicationConstants.PROJECT_CONCLUDE);
				if(projectConclude != null && projectConclude.size() > 0) {
					resultMap.put("projectConclude", projectConclude);
				}				
				return resultMap;				
			}else {
				return null;
			}
		}else {
			return null;
		}
		
	}

	public String getOrderMessage(JSONObject requestObj) {
		String pageIndexStr = requestObj.getAsString("pageIndex");
		int pageSize = Integer.parseInt(env.getProperty("common.pageSize"));
		String objString = requestObj.getAsString("obj");
		JSONObject obj = (JSONObject) JSONValue.parse(objString);
		Integer pageIndex = pageIndexStr == null ? 0:Integer.parseInt(pageIndexStr);
		String orderIdStr = obj.getAsString("orderId");
		Integer orderId = pageIndexStr == null? 0:Integer.parseInt(orderIdStr);		
		Pageable page = PageRequest.of(pageIndex, pageSize, Sort.Direction.DESC,"createTime");
		Page<OrderMessage> orderMessageResult = orderMessageDao.findByOrderId(orderId, page);
		return PageDataWrapUtils.page2JsonNoCopy(orderMessageResult);
	}

	public Orders simpleGet(JSONObject requestObj) {
		Number idNum = requestObj.getAsNumber("id");
		if(idNum != null) {
			Integer ordersId = idNum.intValue();
			Optional<Orders> orders = ordersDao.findById(ordersId);
			return orders.get();
		}else {
			return null;
		}
		
	}
	

	public String assignOrder(JSONObject requestObj) {
		SimpleMessageBody resultBody = new SimpleMessageBody();
		JSONArray idObj = (JSONArray) requestObj.get("id");
		if(idObj != null && idObj.size() > 0) {
			String idStr = idObj.get(0).toString();
			//update item by id 
			Integer id = Integer.parseInt(idStr);
			Optional<Orders> ordersOp =ordersDao.findById(id);
			if(ordersOp.isPresent()) {
				//get all params except id 
				Orders order = ordersOp.get();
				if(TradeStatus.OPENED.getId() == order.getOrderStatusCode()||TradeStatus.CLOSED.getId() == order.getOrderStatusCode()) {
					EntityBeanUtils.copyEntityBeanValuesFromJSON(requestObj, order);
					order.setOrderStatusCode(TradeStatus.ASSIGNED.getId());
					order.setModifyTime(new Date());
					ordersDao.save(order);
					resultBody.setSuccessful(true);
					resultBody.setDescription("指派订单成功");
				}else {
					resultBody.setSuccessful(false);
					resultBody.setDescription("只有已提交或已关闭状态的订单可以指派");
				}				
			}else {
				resultBody.setSuccessful(false);
				resultBody.setDescription("未找到Update的Item");
			}
		}else {
			resultBody.setSuccessful(false);
			resultBody.setDescription("关键参数Id缺失 无法更新");
		}
		return JSONUtils.objToJSON(resultBody);
	}

	public String close(JSONObject requestObj) {
		SimpleMessageBody resultBody = new SimpleMessageBody();
		String idStr = requestObj.getAsString("id");
		Integer id = Integer.parseInt(idStr);
		Optional<Orders> ordersOp =ordersDao.findById(id);
		if(ordersOp.isPresent()) {
			Orders order = ordersOp.get();
			order.setOrderStatusCode(TradeStatus.CLOSED.getId());
			order.setModifyTime(new Date());
			ordersDao.save(order);
		}
		resultBody.setSuccessful(true);
		resultBody.setDescription("关闭订单成功");
		return JSONUtils.objToJSON(resultBody);
	}

	public String add(JSONObject requestObj) {
		JSONObject orderJson = (JSONObject)requestObj.get("order");
		if(orderJson != null) {
			Orders orders = JSONUtils.jsonToObj(orderJson.toJSONString(), Orders.class);
			JSONObject user = (JSONObject)requestObj.get("user");
			PurchaserAccount owner = JSONUtils.jsonToObj(user.toJSONString(), PurchaserAccount.class);
			PurchaserAccount dbOwner = purchaseDao.findByLoginId(owner.getLoginId());
			orders.setOrderOwnerId(dbOwner.getId());
			orders.setOrderNo(OrderNoGenerator.generateOrderNo());
			orders.setOrderStatusCode(TradeStatus.OPENED.getId());
			orders.setCreateTime(new Date());
			ordersDao.saveAndFlush(orders);
			return JSONUtils.objToJSON(orders);
		}else {
			return null;
		}		
	}
	/**
	 * 根据loginId查询订单信息
	 * @param pageIndex
	 * @param loginId
	 * @return
	 */
	public String getOrderListByLoginId( JSONObject requestObj){
		int pageIndex =  (int)requestObj.get("pageIndex");
		String loginId = (String)requestObj.get("loginId");
		Integer pageSize=Integer.parseInt(environment.getProperty("common.pageSize"));
		PurchaserAccount dbAccount = purchaseDao.findByLoginId(loginId);
		String sql = "";
		if(dbAccount != null) {
			Integer role = dbAccount.getSysRoleId();
			if( role == 1) {//采购方
				sql = " and login.login_id='" + loginId + "'";;
			}else {//咨询师
				sql = "  and orders.order_adviser_id='" + dbAccount.getId() +"'";
			}
			List<Map<String,Object>> resultList = ordersDao.getOrdersByLoginId(jpqlExecute, pageSize, pageIndex, sql);
			for(Map<String,Object> map : resultList) {
				String orderAdviserId = (String) map.get("orderAdviserId");
				if( !StringUtils.isEmpty(orderAdviserId)) {
					Integer orderAdviserIdInt = Integer.parseInt(orderAdviserId);
					if(orderAdviserId != null) {
						List<UserInfo> adviser = infoDao.findByLoginInfoId(orderAdviserIdInt);
						if( adviser != null && adviser.get(0)!= null) {
							map.put("orderAdviserName", adviser.get(0).getName());
						}
					}
				}
			}
			Integer total = ordersDao.countAllWithOrdersByLoginId(jpql,sql);
			Pageable page =  PageRequest.of(pageIndex, pageSize, Sort.Direction.ASC, "id");
			PageImpl<Map<String,Object>> result = new PageImpl<Map<String,Object>>(resultList, page, total);
			return PageDataWrapUtils.page2JsonNoCopy(result);
		}
		return "";
	}
	public String saveFileOrderAttach(JSONObject requestObj) {
		Integer orderId = (Integer)requestObj.get("orderId");
		String loginId = (String)requestObj.get("loginId");
		String filePath = (String)requestObj.get("filePath");
		String pagePath = (String)requestObj.get("pagePath");
		if(orderId!=0 && !StringUtils.isEmpty(loginId) && !StringUtils.isEmpty(filePath)) {
			List<OrderAttachments> caseDraft = ordersAttacgDao.findByOrderIdAndPagePath(orderId, pagePath);
			OrderAttachments orderAttach = new OrderAttachments();
			orderAttach.setOrderId(orderId);
			int start = filePath.lastIndexOf("/");
			int end = filePath.indexOf(".");
			String fileName = filePath.substring(start+1, end);
			orderAttach.setFileName(fileName);
			orderAttach.setFilePath(filePath);
			orderAttach.setPagePath(pagePath);
			if( !caseDraft.isEmpty() ) {//已存在就修改
				orderAttach.setId(caseDraft.get(0).getId());
			}else {
				orderAttach.setCreateTime(new Date());
			}
			ordersAttacgDao.saveAndFlush(orderAttach);
			Optional<Orders> orderOp = ordersDao.findById(orderId);
			if(orderOp.isPresent()) {
				Orders order  = orderOp.get();
				order.setModifyTime(new Date());
				ordersDao.save(order);
			}
			
			return "success";

		}
		return "failed";
	}
	public String orderResultClick(JSONObject requestObj) {
		Integer orderId = (Integer)requestObj.get("orderId");
		Integer resultCode = (Integer)requestObj.get("resultCode");
		if( orderId != 0) {
			Optional<Orders>  ordersOp = ordersDao.findById(orderId);
			if(ordersOp.isPresent()) {
				Orders order = ordersOp.get();
				if( order != null ) {
					order.setOrderStatusCode(resultCode);
					ordersDao.saveAndFlush(order);//更新状态
					return "success";
				}
				
			}
		}
		return "failed";
	}
}
