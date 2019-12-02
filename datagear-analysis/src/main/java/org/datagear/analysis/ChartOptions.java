/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.util.Collections;
import java.util.Map;

/**
 * 图表设置项集。
 * 
 * @author datagear@163.com
 *
 */
public class ChartOptions
{
	@SuppressWarnings("unchecked")
	private Map<String, ?> options = Collections.EMPTY_MAP;

	public ChartOptions()
	{
	}

	public ChartOptions(Map<String, ?> options)
	{
		super();
		this.options = options;
	}

	public Map<String, ?> getOptions()
	{
		return options;
	}

	public void setOptions(Map<String, ?> options)
	{
		this.options = options;
	}

	@SuppressWarnings("unchecked")
	public <T> T getOption(String name)
	{
		return (T) (this.options == null ? null : this.options.get(name));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [options=" + options + "]";
	}
}
