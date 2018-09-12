/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.io.Serializable;

import org.datagear.model.Model;
import org.datagear.model.PropertyAccessException;

/**
 * 默认属性。
 * 
 * @author datagear@163.com
 *
 */
public class DefaultProperty extends AbstractProperty implements Serializable
{
	private static final long serialVersionUID = 1L;

	private PropertyValueAccessor propertyValueAccessor;

	public DefaultProperty()
	{
		super();
	}

	public DefaultProperty(String name, Model model, PropertyValueAccessor propertyValueAccessor)
	{
		super(name, model);
		this.propertyValueAccessor = propertyValueAccessor;
	}

	public PropertyValueAccessor getPropertyValueAccessor()
	{
		return propertyValueAccessor;
	}

	public void setPropertyValueAccessor(PropertyValueAccessor propertyValueAccessor)
	{
		this.propertyValueAccessor = propertyValueAccessor;
	}

	@Override
	public Object get(Object obj) throws PropertyAccessException
	{
		return this.propertyValueAccessor.get(obj);
	}

	@Override
	public void set(Object obj, Object value) throws PropertyAccessException
	{
		this.propertyValueAccessor.set(obj, value);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", model=" + getModel() + "]";
	}
}
