/*
 * Copyright 2018-present datagear.tech
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 默认{@linkplain RenderContext}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultRenderContext implements RenderContext, Serializable
{
	private static final long serialVersionUID = 1L;

	private Map<String, Object> map;

	public DefaultRenderContext()
	{
		super();
		this.map = new HashMap<>();
	}

	public DefaultRenderContext(Map<String, Object> map)
	{
		super();
		this.map = map;
	}

	@Override
	public int size()
	{
		return this.map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.map.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		return this.map.get(key);
	}

	@Override
	public Object put(String key, Object value)
	{
		return this.map.put(key, value);
	}

	@Override
	public Object remove(Object key)
	{
		return this.map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.map.putAll(m);
	}

	@Override
	public void clear()
	{
		this.map.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.map.keySet();
	}

	@Override
	public Collection<Object> values()
	{
		return this.map.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		return this.map.entrySet();
	}

	@Override
	public DefaultRenderContext copy()
	{
		DefaultRenderContext re = new DefaultRenderContext();
		putAllInThis(re);

		return re;
	}

	protected void putAllInThis(RenderContext renderContext)
	{
		renderContext.putAll(this);
	}

	protected Map<String, Object> getMap()
	{
		return map;
	}

	protected void setMap(Map<String, Object> map)
	{
		this.map = map;
	}
}
