package com.iwantrun.core.service.application.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iwantrun.core.service.application.annotation.NeedTokenVerify;
import com.iwantrun.core.service.application.domain.Cases;
import com.iwantrun.core.service.application.domain.LocationAttachments;
import com.iwantrun.core.service.application.domain.LocationTags;
import com.iwantrun.core.service.application.domain.Locations;
import com.iwantrun.core.service.application.service.CasesService;
import com.iwantrun.core.service.application.service.LocationsService;
import com.iwantrun.core.service.application.transfer.Message;
import com.iwantrun.core.service.application.transfer.PageDomianRequest;
import com.iwantrun.core.service.application.transfer.SimpleMessageBody;
import com.iwantrun.core.service.utils.DictionaryConfigParams;
import com.iwantrun.core.service.utils.EntityBeanUtils;
import com.iwantrun.core.service.utils.JPADBUtils;
import com.iwantrun.core.service.utils.JSONUtils;
import com.iwantrun.core.service.utils.ListUpdateUtils;
import com.iwantrun.core.service.utils.MappingGenerateUtils;
import com.iwantrun.core.service.utils.PageDataWrapUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@RestController
public class CaseController {
	
	Logger logger = LoggerFactory.getLogger(CaseController.class);
	
	@Autowired
	private CasesService casesService;
	

/*	@SuppressWarnings("unchecked")
	@RequestMapping("/application/location/add")
	@NeedTokenVerify
	public Message addLocation(@RequestBody Message message) {
		Message response = new Message();
		response.setAccessToken(message.getAccessToken());
		response.setRequestMethod(message.getRequestMethod());
		Locations location = new Locations();
		String dataJson = message.getMessageBody();
		JSONObject object = (JSONObject) JSONValue.parse(dataJson);
		Map<String,String> mappingRelation = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"name =>name",
						"activeTypeCode=>activity_type_code",
						"locationTypeCode=>location_type_code",
						"groupNumberLimitCode=>group_number_limit_code",
						"activityProvinceCode=>activity_province_code",
						"activityCityCode=>activity_city_code",
						"activityDistCode=>activity_dist_code",
						"location=>location",
						"priority=>priority",
						"descirbeText1=>_ue",
						"descirbeText2=>mainImage"
				});
		EntityBeanUtils.beanCreateFromJson(location, mappingRelation, object);
		List<LocationAttachments> locationAttachments = new ArrayList<LocationAttachments>();
		Map<String,String> mappingRelation0 = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"filePath=>bean"
				});
		JSONArray array = (JSONArray) object.get("imgManage[]");		
		EntityBeanUtils.listBeanCreateFromJson(locationAttachments, mappingRelation0, array, LocationAttachments.class);
		Function<String,String> fun = s -> {
			int index = s.lastIndexOf("/");
			return s.substring(index+1);
		} ;
		
		BiFunction<String,Integer,String> biFun = (value,index) -> value+"-"+index ;
		ListUpdateUtils.updateListProperty(locationAttachments, 
				new String[] {
						"filePath=>fileName"
				}, 
				new Function[] {
						fun
				}
				, new String[] {
						"pagePath==sideImage"
				}
		 		, (BiFunction<String,Integer,String>[])new BiFunction[]{
						biFun
				} 
		);
		JSONArray tags = (JSONArray) object.get("special_tags[]");
		Map<String,String> mappingRelation1 = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"tagsCode=>bean"
				});
		List<LocationTags> tagsList = new ArrayList<LocationTags>();
		EntityBeanUtils.listBeanCreateFromJson(tagsList, mappingRelation1, tags, LocationTags.class);
		Supplier<Integer> tagsTypeSupplier = () ->{
			return DictionaryConfigParams.LOCATION_TAGS_TYPE;
		};
		ListUpdateUtils.updateListPropertyWithSupplier(tagsList, new String[]{
				"tagsType"
		}, new Supplier[] {
			tagsTypeSupplier
		});
		boolean updateResult =locationService.createLocations(location, locationAttachments,tagsList);
		if(updateResult) {			
			response.setMessageBody(String.valueOf(location.getId()));
		}else {
			response.setMessageBody("failed");
		}
		return response;
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/application/location/modify")
	@NeedTokenVerify
	public Message modifyLocation(@RequestBody Message message) {
		String dataJson = message.getMessageBody();
		logger.info("received json:"+dataJson);
		Locations location = new Locations();
		JSONObject object = (JSONObject) JSONValue.parse(dataJson);
		Map<String,String> mappingRelation = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"id=>id",
						"name =>name",
						"activeTypeCode=>activity_type_code",
						"locationTypeCode=>location_type_code",
						"groupNumberLimitCode=>group_number_limit_code",
						"activityProvinceCode=>activity_province_code",
						"activityCityCode=>activity_city_code",
						"activityDistCode=>activity_dist_code",
						"location=>location",
						"priority=>priority",
						"descirbeText1=>_ue",
						"descirbeText2=>mainImage"
				});
		EntityBeanUtils.beanCreateFromJson(location, mappingRelation, object);
		List<LocationAttachments> locationAttachments = new ArrayList<LocationAttachments>();
		Map<String,String> mappingRelation0 = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"filePath=>bean"
				});
		JSONArray array = (JSONArray) object.get("imgManage[]");		
		EntityBeanUtils.listBeanCreateFromJson(locationAttachments, mappingRelation0, array, LocationAttachments.class);
		Function<String,String> fun = s -> {
			int index = s.lastIndexOf("/");
			return s.substring(index+1);
		} ;
		
		BiFunction<String,Integer,String> biFun = (value,index) -> value+"-"+index ;
		ListUpdateUtils.updateListProperty(locationAttachments, 
				new String[] {
						"filePath=>fileName"
				}, 
				new Function[] {
						fun
				}
				, new String[] {
						"pagePath==sideImage"
				}
		 		, (BiFunction<String,Integer,String>[])new BiFunction[]{
						biFun
				} 
		);
		JSONArray tags = (JSONArray) object.get("special_tags[]");
		Map<String,String> mappingRelation1 = 
				MappingGenerateUtils.generateMappingRelation(new String[] {
						"tagsCode=>bean"
				});
		List<LocationTags> tagsList = new ArrayList<LocationTags>();
		EntityBeanUtils.listBeanCreateFromJson(tagsList, mappingRelation1, tags, LocationTags.class);
		Supplier<Integer> tagsTypeSupplier = () ->{
			return DictionaryConfigParams.LOCATION_TAGS_TYPE;
		};
		ListUpdateUtils.updateListPropertyWithSupplier(tagsList, new String[]{
				"tagsType"
		}, new Supplier[] {
			tagsTypeSupplier
		});
		SimpleMessageBody updateResult =casesService.modifyLocations(location, locationAttachments,tagsList);
		message.setMessageBody(JSONValue.toJSONString(updateResult));
		return message;
	}*/
	
	@RequestMapping("/application/cases/findAll")
	public Message findAll(@RequestBody Message message) {
		String dataJson = message.getMessageBody();
		JSONObject object = (JSONObject) JSONValue.parse(dataJson);
		String pageIndex = object.getAsString("pageIndex");
		Page<Cases> resultPage =casesService.findAllCasesPageable(Integer.parseInt(pageIndex));
		message.setMessageBody(PageDataWrapUtils.page2Json(resultPage));
		return message;		
	}
	
	/*@RequestMapping("/application/location/findByExample")
	public Message findByExample(@RequestBody Message message) {
		String dataJson = message.getMessageBody();
		PageDomianRequest example = JSONUtils.jsonToObj(dataJson, PageDomianRequest.class);
		Page<Locations> resultPage = null ;
		if(!example.getObj().containsKey("*")) {
			resultPage =locationService.queryLocationByConditionPageable(example.getPageIndex(), example.getObjAsType(Locations.class));
		}else {
			String value = (String) example.getObj().get("*");
			Locations defaultLocation = new Locations();
			defaultLocation.setName(value);
			defaultLocation.setLocation(value);
			String[] defaultSpecification = new String[] {
					"like,name,or",
					"like,location,or",
			};
			Specification<Locations> specification = JPADBUtils.generateSpecificationFromExample(defaultLocation, defaultSpecification);
			resultPage = locationService.queryLocationBySpecificationPageable(example.getPageIndex(), specification);
		}
		
		message.setMessageBody(PageDataWrapUtils.page2Json(resultPage));
		return message;		
	}
	
	@RequestMapping("/application/location/delete")
	@NeedTokenVerify
	public Message delete(@RequestBody Message message) {
		String id = message.getMessageBody();
		String result = locationService.delete(id);
		message.setMessageBody(String.valueOf(result));
		return message ;
	}
	
	@RequestMapping("/application/location/get")
	public Message get(@RequestBody Message message) {
		String jsonId = message.getMessageBody();
		JSONObject objectId = (JSONObject) JSONValue.parse(jsonId);
		Integer id = Integer.parseInt(objectId.getAsString("id"));
		String result = locationService.get(id);
		message.setMessageBody(result);
		return message;
	}*/
	

}
