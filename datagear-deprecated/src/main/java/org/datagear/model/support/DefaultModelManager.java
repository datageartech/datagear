/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.datagear.model.Model;
import org.datagear.model.ModelManager;

/**
 * 默认模型管理器。
 * 
 * @author zzf
 * @createDate 2015年4月19日
 * 
 */
public class DefaultModelManager implements ModelManager
{
	private Map<String, Model> modelMap = new HashMap<String, Model>();

	public DefaultModelManager()
	{
		super();
	}

	public DefaultModelManager(Model... models)
	{
		super();

		for (Model model : models)
			this.modelMap.put(model.getName(), model);
	}

	public DefaultModelManager(Collection<? extends Model> models)
	{
		super();

		for (Model model : models)
			this.modelMap.put(model.getName(), model);
	}

	@Override
	public int size()
	{
		return this.modelMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.modelMap.isEmpty();
	}

	@Override
	public boolean contains(String name)
	{
		return this.modelMap.containsKey(name);
	}

	@Override
	public Model get(String name)
	{
		return this.modelMap.get(name);
	}

	@Override
	public Model put(Model model)
	{
		return this.modelMap.put(model.getName(), model);
	}

	@Override
	public void putAll(Model[] models)
	{
		for (Model model : models)
			this.modelMap.put(model.getName(), model);
	}

	@Override
	public void putAll(Collection<? extends Model> models)
	{
		for (Model model : models)
			this.modelMap.put(model.getName(), model);
	}

	@Override
	public Model remove(String name)
	{
		return this.modelMap.remove(name);
	}

	@Override
	public void clear()
	{
		this.modelMap.clear();
	}

	@Override
	public Collection<Model> toCollection()
	{
		return Collections.unmodifiableCollection(this.modelMap.values());
	}

	@Override
	public Map<String, Model> toMap()
	{
		return new HashMap<String, Model>(this.modelMap);
	}
}
