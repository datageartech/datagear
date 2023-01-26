/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认{@linkplain RenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultRenderContext implements RenderContext
{
	private Map<String, Object> attributes;

	public DefaultRenderContext()
	{
		super();
		this.attributes = new HashMap<>();
	}

	public DefaultRenderContext(Map<String, ?> attributes)
	{
		super();
		this.setAttributes(attributes);
	}

	public DefaultRenderContext(DefaultRenderContext renderContext)
	{
		super();
		this.attributes = renderContext.attributes;
	}

	public Map<String, Object> getAttributes()
	{
		return attributes;
	}

	@SuppressWarnings("unchecked")
	public void setAttributes(Map<String, ?> attributes)
	{
		this.attributes = (Map<String, Object>)attributes;
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

	@Override
	public void putAttributes(Map<String, ?> attrs)
	{
		if(attrs != null)
			this.attributes.putAll(attrs);
	}

	@Override
	public void putAttributes(RenderContext renderContext)
	{
		if(renderContext != null)
			putAttributes(renderContext.getAttributes());
	}
}
