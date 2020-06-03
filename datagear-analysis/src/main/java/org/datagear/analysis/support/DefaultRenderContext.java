/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.RenderContext;

/**
 * 抽象{@linkplain RenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultRenderContext implements RenderContext
{
	private Map<String, Object> attributes = new HashMap<>();

	public DefaultRenderContext()
	{
		super();
	}

	public DefaultRenderContext(Map<String, ?> attributes)
	{
		super();
		this.attributes.putAll(attributes);
	}

	public DefaultRenderContext(RenderContext renderContext)
	{
		super();

		Map<String, ?> attributes = renderContext.getAttributes();
		if (attributes != null)
			this.attributes.putAll(attributes);
	}

	public void setAttributes(Map<String, Object> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public Map<String, ?> getAttributes()
	{
		return this.attributes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name)
	{
		return (T) this.attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value)
	{
		this.attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeAttribute(String name)
	{
		return (T) this.attributes.remove(name);
	}

	@Override
	public boolean hasAttribute(String name)
	{
		return this.attributes.containsKey(name);
	}
}
