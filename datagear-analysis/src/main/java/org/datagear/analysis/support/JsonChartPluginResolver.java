/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.Reader;
import java.util.Map;

import org.datagear.analysis.ChartPlugin;

/**
 * JSON {@linkplain ChartPlugin}解析器。
 * <p>
 * 此类从JSON解析{@linkplain ChartPlugin}对象的属性：
 * </p>
 * <p>
 * <code>
 * {<br/>
 * 	id : "...",<br/>
 * 	nameLabel : { value : "...", localeValues : { "zh" : "...", "en" : "..." }},<br/>
 * 	descLabel : { ... },<br/>
 * 	manualLabel : { ... },<br/>
 * 	icons : { "LIGHTNESS" : { location : "classpath:/.../.../icon.png" }, "DARK" : { location : "file:/.../.../icon.png" } },<br/>
 * 	chartProperties :  [ { ... }, ... ]<br/>
 * }
 * </code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonChartPluginResolver
{
	public JsonChartPluginResolver()
	{
		super();
	}

	/**
	 * 从JSON输入流解析{@linkplain ChartPlugin}。
	 * 
	 * @param reader
	 * @return
	 */
	public Map<String, Object> resolveChartPluginProperties(Reader reader)
	{
		// TODO
		return null;
	}
}
