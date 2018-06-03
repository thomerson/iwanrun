package com.iwantrun.core.service.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.iwantrun.core.constant.AdminApplicationConstants;
import com.iwantrun.core.service.application.dao.JPQLEnableRepository;
import com.iwantrun.core.service.application.dao.PurchaserAccountDao;
import com.iwantrun.core.service.application.dao.UserInfoAttachmentsDao;
import com.iwantrun.core.service.application.dao.UserInfoDao;
import com.iwantrun.core.service.application.domain.PurchaserAccount;
import com.iwantrun.core.service.application.domain.UserInfo;
import com.iwantrun.core.service.application.domain.UserInfoAttachments;
import com.iwantrun.core.service.application.transfer.MixedUserResponse;
import com.iwantrun.core.service.application.transfer.PurchaserAccountRequest;
import com.iwantrun.core.service.application.transfer.SimpleMessageBody;
import com.iwantrun.core.service.utils.JSONUtils;
import com.iwantrun.core.service.utils.Md5Utils;
import com.iwantrun.core.service.utils.PageDataWrapUtils;

import net.minidev.json.JSONObject;

@Service
public class PurchaserAccountService {
	@Autowired
	private Environment environment;
	@Autowired
	private PurchaserAccountDao dao;
	@Autowired
	private UserInfoDao infoDao;
	@Autowired
	private UserInfoAttachmentsDao attachmentsDao;
	@Autowired
	private JPQLEnableRepository jpqlExecute;

	public String register(PurchaserAccount account) {
		String md5Password=Md5Utils.generate(account.getPassword());
		account.setPassword(md5Password);
		PurchaserAccount saved = dao.save(account);
		if (saved == null) {
			return "数据保存失败，请重试";
		}
		return null;
	}

	public String validateRegisterParam(PurchaserAccountRequest accountRequest) {
		if (accountRequest == null || accountRequest.getAccount() == null) {
			return "请输入相关数据";
		}
		String smsCode = accountRequest.getSmsCode();
		if (StringUtils.isEmpty(smsCode)) {
			return "短信验证码不能为空";
		}
		// 短信验证码校验
		// String validated = validate(smsCode)

		String password = accountRequest.getAccount().getPassword();
		boolean matchered = password.matches(environment.getProperty("purchaser.account.password.regex"));

		if (!matchered) {
			return "密码格式错误，请重新输入";
		}
		return null;
	}

	public String validateLoginParam(PurchaserAccount account) {
		String loginId=account.getLoginId();
		String password=account.getPassword();
		if(StringUtils.isEmpty(loginId)) {
			return "请输入账号";
		}
		if(StringUtils.isEmpty(password)) {
			return "请输入密码";
		}
		
		String md5Passrword=Md5Utils.generate(password);
		
		PurchaserAccount find= dao.findByLoginIdAndPassword(loginId,md5Passrword);
		
		if(find==null) {
			return "账号密码不匹配";
		}
		return null;
	}
	
	public String findPurchaseUserPaged(JSONObject obj) {
		Integer pageSize = Integer.parseInt(environment.getProperty("common.pageSize"));
		String index = obj.getAsString("pageIndex");
		int pageIndex =  index == null ? 1:Integer.parseInt(index)+1 ;
		String loginId = obj.getAsString("loginId");
		String name = obj.getAsString("name");
		String mobileNumber = obj.getAsString("mobileNumber");
		Integer role = obj.getAsNumber("role") == null ?null : obj.getAsNumber("role").intValue();
		Pageable page =  PageRequest.of(pageIndex-1, pageSize, Sort.Direction.ASC, "id");
		Long totalNum = dao.countByMutipleParams(loginId, name, role, mobileNumber, jpqlExecute);
		List<MixedUserResponse> content = dao.findByMutipleParams(loginId, name, role, mobileNumber, jpqlExecute, pageSize, pageIndex);
		PageImpl<MixedUserResponse> result = new PageImpl<MixedUserResponse>(content, page, totalNum);
		return PageDataWrapUtils.page2JsonNoCopy(result);
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(rollbackOn=Exception.class)
	public String addPurchaseUserAndRelated(Map<String, Object> paramsMap) {
		String mobileNumber = (String) paramsMap.get("mobileNumber");
		List<PurchaserAccount> findList = dao.findByMobileNumber(mobileNumber);
		// check mobile number not occupy
		if (findList != null && findList.size() > 0) {
			// mobileNumber already taken
			SimpleMessageBody result = new SimpleMessageBody();
			result.setSuccessful(false);
			result.setDescription("手机号已被注册请选择其他手机号");
			return JSONUtils.objToJSON(result);
		} else {
			// combine PurchaserAccount
			PurchaserAccount account = new PurchaserAccount();
			account.setMobileNumber(mobileNumber);
			account.setLoginId(mobileNumber);
			if (paramsMap.get("password") != null) {
				String password = Md5Utils.generate(paramsMap.get("password").toString());
				account.setPassword(password);
			}
			if (paramsMap.get("wec") != null) {
				account.setWec((String) paramsMap.get("wec"));
			}
			if (paramsMap.get("aliPayId") != null) {
				account.setAliPayId((String) paramsMap.get("aliPayId"));
			}
			if (paramsMap.get("role") != null) {
				account.setSysRoleId(Integer.parseInt(paramsMap.get("role").toString()));
			}
			if (paramsMap.get("thirdPartyId1") != null) {
				account.setThirdPartyId1(paramsMap.get("thirdPartyId1").toString());
			}
			if (paramsMap.get("thirdPartyId2") != null) {
				account.setThirdPartyId2(paramsMap.get("thirdPartyId2").toString());
			}
			if (paramsMap.get("thirdPartyId3") != null) {
				account.setThirdPartyId3(paramsMap.get("thirdPartyId3").toString());
			}
			account.setStatus(2);
			dao.saveAndFlush(account);
			int accountLoginId = account.getId();
			// combine userinfo
			UserInfo userInfo = new UserInfo();
			userInfo.setLoginInfoId(accountLoginId);
			userInfo.setName(paramsMap.get("name").toString());
			List<String> genderList = (List<String>) paramsMap.get("gender[]");
			if (genderList != null) {
				userInfo.setGender(Integer.parseInt(genderList.get(0)));
			}
			if (paramsMap.get("contractMobile") != null) {
				userInfo.setContractMobile(paramsMap.get("contractMobile").toString());
			}
			if (paramsMap.get("email") != null) {
				userInfo.setEmail(paramsMap.get("email").toString());
			}
			if (paramsMap.get("companyTypeId") != null) {
				userInfo.setCompanyTypeId(Integer.parseInt(paramsMap.get("companyTypeId").toString()));
			}
			if (paramsMap.get("companySizeId") != null) {
				userInfo.setCompanySizeId(Integer.parseInt(paramsMap.get("companySizeId").toString()));
			}
			userInfo.setVerified(2);
			infoDao.saveAndFlush(userInfo);
			// save attachments
			if (paramsMap.get("imgManage[]") != null) {
				List<String> attchPaths = (List<String>) paramsMap.get("imgManage[]");
				List<UserInfoAttachments> attchList = new ArrayList<UserInfoAttachments>();
				for (String path : attchPaths) {
					UserInfoAttachments attach = new UserInfoAttachments();
					int fileNameIndex = path.lastIndexOf("/");
					attach.setFileName(path.substring(fileNameIndex + 1));
					attach.setFilePath(path);
					attach.setPagePath(AdminApplicationConstants.USER_COMPANY_CREDENTIAL);
					attach.setUserInfoId(userInfo.getId());
					attchList.add(attach);
				}
				attachmentsDao.saveAll(attchList);
			}
		}
		SimpleMessageBody result = new SimpleMessageBody();
		result.setSuccessful(true);
		result.setDescription("添加成功");
		return JSONUtils.objToJSON(result);
	}
}
