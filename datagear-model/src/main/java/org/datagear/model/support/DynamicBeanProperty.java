/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.support;

import java.io.Serializable;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.PropertyAccessException;

/**
 * 适用于{@linkplain DynamicBean}的{@linkplain Property}。
 * 
 * @author datagear@163.com
 *
 */
public class DynamicBeanProperty extends AbstractProperty implements Serializable
{
	private static final long serialVersionUID = 1L;

	public DynamicBeanProperty()
	{
		super();
	}

	public DynamicBeanProperty(String name, Model model)
	{
		super(name, model);
	}

	@Override
	public Object get(Object obj) throws PropertyAccessException
	{
		if (obj instanceof DynamicBean)
			return ((DynamicBean) obj).get(getName());
		else
			throw new PropertyAccessException("The Bean type must be [" + DynamicBean.class.getName() + "]");
	}

	@Override
	public void set(Object obj, Object value) throws PropertyAccessException
	{
		if (obj instanceof DynamicBean)
			((DynamicBean) obj).put(getName(), value);
		else
			throw new PropertyAccessException("The Bean type must be [" + DynamicBean.class.getName() + "]");
	}
}
