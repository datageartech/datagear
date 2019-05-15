/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.Collection;

import org.datagear.model.Model;
import org.datagear.model.Property;

/**
 * 抽象{@linkplain Property}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractProperty extends AbstractFeatured implements Property
{
	/** 属性名称 */
	private String name;

	/** 属性模型 */
	private Model model;

	/** 是否是数组 */
	private boolean array;

	/** 集合类型 */
	@SuppressWarnings("rawtypes")
	private Class<? extends Collection> collectionType;

	/** 默认值 */
	private Object defaultValue;

	public AbstractProperty()
	{
		super();
	}

	public AbstractProperty(String name, Model model)
	{
		super();
		this.name = name;
		this.model = model;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * 设置名称。
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public Model getModel()
	{
		return this.model;
	}

	/**
	 * 设置模型。
	 * 
	 * @param model
	 */
	public void setModel(Model model)
	{
		this.model = model;
	}

	@Override
	public boolean isArray()
	{
		return this.array;
	}

	public void setArray(boolean array)
	{
		this.array = array;
	}

	@Override
	public boolean isCollection()
	{
		return (this.collectionType != null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class<? extends Collection> getCollectionType()
	{
		return this.collectionType;
	}

	public void setCollectionType(@SuppressWarnings("rawtypes") Class<? extends Collection> collectionType)
	{
		this.collectionType = collectionType;
	}

	@Override
	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", model=" + model + "]";
	}
}
