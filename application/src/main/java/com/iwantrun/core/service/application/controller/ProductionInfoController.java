package com.iwantrun.core.service.application.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwantrun.core.service.application.annotation.NeedTokenVerify;
import com.iwantrun.core.service.application.domain.ProductionInfo;
import com.iwantrun.core.service.application.service.ProductionInfoService;
import com.iwantrun.core.service.application.transfer.Message;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@RestController
public class ProductionInfoController {
	Logger logger = LoggerFactory.getLogger(LocationsController.class);

	@Autowired
	ProductionInfoService productionInfoService;

	public void setProductionInfoService(ProductionInfoService productionInfoService) {
		this.productionInfoService = productionInfoService;
	}

	/**
	 * 按照指定的字段筛选、查找产品，如活动类型、天数、人数、参考价格等 
	 * 按照指定的字段对产品列表进行排序，如访问热度、上架时间、参考价格等
	 */
	@RequestMapping("/application/productionInfo/find")
	@NeedTokenVerify
	public Page<ProductionInfo> findByParam(@RequestBody Message message) {

		JSONObject body = (JSONObject) JSONValue.parse(message.getMessageBody());

		Number activityTypeCode = body.getAsNumber("activityTypeCode");
		Number during = body.getAsNumber("during");
		Number groupNumber = body.getAsNumber("groupNumber");
		Number orderSimulatePriceCode = body.getAsNumber("orderSimulatePriceCode");
		Number orderGroupPriceCode = body.getAsNumber("orderGroupPriceCode");
		Number pageNum = body.getAsNumber("pageNum");
		Number pageSize = body.getAsNumber("pageSize");
		String sortFlag = body.getAsString("sortFlag");

		ProductionInfo param = new ProductionInfo();

		if (null != activityTypeCode) {
			param.setActivityTypeCode(activityTypeCode.intValue());
		}
		if (null != during) {
			param.setDuring(during.intValue());
		}
		if (null != groupNumber) {
			param.setGroupNumber(groupNumber.intValue());
		}
		if (null != orderSimulatePriceCode) {
			param.setOrderSimulatePriceCode(orderSimulatePriceCode.intValue());
		}
		if (null != orderGroupPriceCode) {
			param.setOrderGroupPriceCode(orderGroupPriceCode.intValue());
		}
		if (pageNum == null || pageNum.intValue() < 0) {
			pageNum = 0;
		}
		if (pageSize == null || pageSize.intValue() < 0) {
			pageSize = 10;
		}

		Pageable page;
		if (StringUtils.isEmpty(sortFlag)) {
			page = PageRequest.of(pageNum.intValue(), pageSize.intValue());
		} else {
			page = PageRequest.of(pageNum.intValue(), pageSize.intValue(),
					Direction.fromString(body.getAsString("direction")), sortFlag);
		}
		System.err.println(productionInfoService);
		return productionInfoService.findAllByParam(param, page);
	}

	/**
	 * 产品详细介绍
	 */
	@RequestMapping("/application/productionInfo/detail")
	@NeedTokenVerify
	public ProductionInfo detail(@RequestBody Message message) {
		JSONObject body = (JSONObject) JSONValue.parse(message.getMessageBody());
		Number id = body.getAsNumber("id");
		if (id == null) {
			return null;
		}
		return productionInfoService.findById(id.intValue());
	}
	/**
	 * 收藏产品
	 * 将当前产品加入到【我的收藏】中
	 */
	@RequestMapping("/application/productionInfo/collect")
	@NeedTokenVerify
	public boolean collect(@RequestBody Message message) {
		JSONObject body = (JSONObject) JSONValue.parse(message.getMessageBody());
		Number id = body.getAsNumber("id");
		if (id == null) {
			return false;
		}
		return true;
	}
	/**
	 * 	分享产品
	 *  通过生成二维码扫码的方式将产品信息分享到微信好友或微信朋友圈
	 */
	@RequestMapping("/application/productionInfo/share")
	@NeedTokenVerify
	public boolean share(@RequestBody Message message) {
		JSONObject body = (JSONObject) JSONValue.parse(message.getMessageBody());
		Number id = body.getAsNumber("id");
		if (id == null) {
			return false;
		}
		return true;
	}
}