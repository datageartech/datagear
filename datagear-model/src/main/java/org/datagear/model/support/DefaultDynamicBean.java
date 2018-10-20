/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.datagear.model.Model;

/**
 * 默认{@link DynamicBean}。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultDynamicBean implements DynamicBean
{
	private Model model;

	private Map<String, Object> properties = new HashMap<String, Object>();

	public DefaultDynamicBean()
	{
		super();
	}

	public DefaultDynamicBean(Model model)
	{
		super();
		this.model = model;
	}

	public DefaultDynamicBean(Model model, Map<String, Object> properties)
	{
		super();
		this.model = model;
		this.properties = properties;
	}

	@Override
	public int size()
	{
		return this.properties.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.properties.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.properties.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.properties.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		return this.properties.get(key);
	}

	@Override
	public Object put(String key, Object value)
	{
		return this.properties.put(key, value);
	}

	@Override
	public Object remove(Object key)
	{
		return this.properties.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.properties.putAll(m);
	}

	@Override
	public void clear()
	{
		this.properties.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.properties.keySet();
	}

	@Override
	public Collection<Object> values()
	{
		return this.properties.values();
	}

	@Override
	public Set<Entry<String, Object>> entrySet()
	{
		return this.properties.entrySet();
	}

	@Override
	public void setModel(Model model)
	{
		this.model = model;
	}

	@Override
	public Model getModel()
	{
		return this.model;
	}

	// XXX 移除了hashCode, equals, toString方法，因为如果有循环引用的属性，这几个方法会出现死循环
}
