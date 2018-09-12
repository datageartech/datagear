/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.json.jackson;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.datagear.model.support.DefaultDynamicBean;
import org.datagear.model.support.DynamicBean;

/**
 * 用于创建{@linkplain ObjectMapper}的工厂类。
 * 
 * @author datagear@163.com
 *
 */
public class ObjectMapperFactory
{
	public ObjectMapperFactory()
	{
		super();
	}

	/**
	 * 获取{@linkplain ObjectMapper}。
	 * 
	 * @return
	 */
	public ObjectMapper getObjectMapper()
	{
		ObjectMapper mapper = new ObjectMapper();

		SimpleModule module = new SimpleModule("dynamicBeanJsonSerializer", new Version(0, 1, 0, ""));
		module.addSerializer(DynamicBean.class, new DynamicBeanJsonSerializer());
		module.addSerializer(DefaultDynamicBean.class, new DynamicBeanJsonSerializer());
		mapper.registerModule(module);

		return mapper;
	}

	/**
	 * 获取{@linkplain ObjectMapper}，它不会在写完之后关闭目标输出流。
	 * <p>
	 * 可用于JSP页面内的JSON输出，因为页面内不能关闭JSP输出流。
	 * </p>
	 * 
	 * @return
	 */
	public ObjectMapper getObjectMapperNotCloseTarget()
	{
		ObjectMapper objectMapper = getObjectMapper();

		objectMapper.configure(org.codehaus.jackson.JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

		return objectMapper;
	}
}
