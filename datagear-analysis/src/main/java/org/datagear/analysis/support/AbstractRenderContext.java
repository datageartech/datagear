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
public abstract class AbstractRenderContext implements RenderContext
{
	private Map<String, Object> attributes;

	public AbstractRenderContext()
	{
		super();
		this.attributes = new HashMap<String, Object>();
	}

	@SuppressWarnings("unchecked")
	public AbstractRenderContext(Map<String, ?> attributes)
	{
		super();
		this.attributes = (Map<String, Object>) attributes;
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
		return (T) (this.attributes == null ? null : this.attributes.get(name));
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
