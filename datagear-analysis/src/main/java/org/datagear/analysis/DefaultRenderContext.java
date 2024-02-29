/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认{@linkplain RenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultRenderContext implements RenderContext, Serializable
{
	private static final long serialVersionUID = 1L;

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

	@Override
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
