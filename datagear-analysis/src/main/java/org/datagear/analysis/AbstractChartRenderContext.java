/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象{@linkplain ChartRenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartRenderContext implements ChartRenderContext
{
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public AbstractChartRenderContext()
	{
	}

	public void setAttributes(Map<String, Object> attributes)
	{
		this.attributes = attributes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name)
	{
		return (T) (this.attributes == null ? null : this.attributes.get(name));
	}

	@Override
	public void setAttribute(String name, Object value)
	{
		this.attributes.put(name, value);
	}

	@Override
	public boolean hasAttribute(String name)
	{
		return this.attributes.containsKey(name);
	}

	@Override
	public Map<String, ?> getAttributes()
	{
		return this.attributes;
	}
}
