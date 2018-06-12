package com.iwantrun.core.service.application.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iwantrun.core.service.application.config.DictionaryPageConfig;
import com.iwantrun.core.service.application.dao.DictionaryDao;
import com.iwantrun.core.service.application.domain.Dictionary;

@Service
public class DictionaryService {
	
	Logger logger = LoggerFactory.getLogger(DictionaryService.class);
	
	@Autowired
	private DictionaryDao dictionaryDao ;	
	
	public String getPages() {
		return DictionaryPageConfig.getPageConfigJson();
	}

	public String getTabs(String name) {
		return DictionaryPageConfig.getPageTages(name);
	}

	public List<Dictionary> findByCondition(String filedId,String name) {
		Dictionary example = new Dictionary();
		example.setUsed_field(filedId);
		example.setName(name);
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("used_field", GenericPropertyMatchers.exact())
				.withMatcher("name", GenericPropertyMatchers.exact())
				.withIgnorePaths("id","display_code","display_value","code","value","assignTo");
				;
		return dictionaryDao.findAll(Example.of(example,matcher));
	}
	
	public Dictionary findByCondition(String filedId,String name,int code) {
		Dictionary example = new Dictionary();
		example.setUsed_field(filedId);
		example.setName(name);
		example.setCode(code);
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("used_field", GenericPropertyMatchers.exact())
				.withMatcher("name", GenericPropertyMatchers.exact())
				.withMatcher("code", GenericPropertyMatchers.exact())
				.withIgnorePaths("id","display_code","display_value","value","assignTo");
		 Optional<Dictionary> op = dictionaryDao.findOne(Example.of(example,matcher));
		 if(op.isPresent()) {
			 return op.get();
		 }else {
			 return null;
		 }
				
	}

	@Transactional
	public Dictionary add(Dictionary obj) {
		Dictionary dbRecord = this.findByCondition(obj.getUsed_field(), obj.getName(), obj.getCode());
		if(dbRecord != null) {
			dbRecord.setValue(obj.getValue());
			return dictionaryDao.saveAndFlush(dbRecord);
		}else {
			return dictionaryDao.saveAndFlush(obj);
		}		
	}
	
	@Transactional
	public void delete(int id) {
		dictionaryDao.deleteById(id);
	}
	
	public <T> List<T> dictionaryFilter(List<T> filterList,Map<String,Dictionary> filterMap){
		if(filterMap == null || filterList == null) {
			return filterList ;
		}else {
			Set<String> properties = filterMap.keySet();
			Map<String,Map<Integer,String>> propertyCodeValueMap = new HashMap<String,Map<Integer,String>>();
			//准备code value 映射 在进行fiterList迭代的时候进行匹配
			for(String property : properties) {
				Map<Integer,String> codeValueMap = new HashMap<Integer,String>();
				Dictionary example = filterMap.get(property);
				ExampleMatcher matcher = ExampleMatcher.matchingAll()
						.withMatcher("used_field", GenericPropertyMatchers.exact())
						.withMatcher("name", GenericPropertyMatchers.exact())
						.withIgnorePaths("id","display_code","display_value","value","code","assignTo");
				List<Dictionary> codeValueList  = dictionaryDao.findAll(Example.of(example,matcher));
				for(Dictionary dic : codeValueList) {
					codeValueMap.put(dic.getCode(), dic.getValue());
				}
				propertyCodeValueMap.put(property, codeValueMap);
			}
			for(T item : filterList) {
				for(String property : properties) {
					try {
						String code = (String) PropertyUtils.getSimpleProperty(item, property);
						Integer codeInt = Integer.parseInt(code);
						Map<Integer,String> codeValueMap = propertyCodeValueMap.get(property);
						String value = codeValueMap.get(codeInt);
						PropertyUtils.setProperty(item, property, value);
					} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						logger.error("数据字典数据转化的时候发生错误",e);
					}
				}
			}
			return filterList;
		}
	}

	public List<Dictionary> findByAssign(String assignTo) {
		Dictionary example = new Dictionary();
		example.setAssignTo(assignTo);
		ExampleMatcher matcher = ExampleMatcher.matchingAll().withMatcher("assignTo", GenericPropertyMatchers.exact())
				.withIgnorePaths("id","display_code","display_value","code","value","used_field","name");
		return dictionaryDao.findAll(Example.of(example,matcher));
	}
	public List<Dictionary> findDictionaryByName(String name) {
		Dictionary example = new Dictionary();
		example.setName(name);
		ExampleMatcher matcher = ExampleMatcher.matchingAll()
				.withMatcher("name", GenericPropertyMatchers.exact())
				.withIgnorePaths("id","display_code","display_value","code","value","assignTo");
				
		return dictionaryDao.findAll(Example.of(example,matcher));
	}

}
