/*
 * Copyright 2018-present datagear.tech
 */

package org.datagear.util;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * {@linkplain Properties}工具类。
 * 
 * @author datagear@163.com
 *
 */
public class PropertiesUtil
{
	/**
	 * 加载{@linkplain Properties}，其中的<code>${...}</code>将被处理替换。
	 * 
	 * @param resource
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static Properties loadProperties(Resource resource, String charset) throws IOException
	{
		Properties re = new Properties();
		loadProperties(re, resource, charset);
		return re;
	}

	/**
	 * 加载并写入{@linkplain Properties}，其中的<code>${...}</code>将被处理替换。
	 * 
	 * @param properties
	 *            写入对象
	 * @param resource
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static void loadProperties(Properties properties, Resource resource, String charset) throws IOException
	{
		Properties origin = PropertiesLoaderUtils.loadProperties(new EncodedResource(resource, charset));
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");

		Set<String> names = origin.stringPropertyNames();
		for (String name : names)
		{
			String value = origin.getProperty(name);
			value = helper.replacePlaceholders(value, origin);
			properties.setProperty(name, value);
		}
	}
}
