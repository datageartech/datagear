/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端具象列值。
 * <p>
 * 它可结合{@linkplain PropertyConcreteColumnName}，用于自定义属性端具象列值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyConcreteColumnValue extends ValueFeature<Object> implements PropertyFeature, PersistenceFeature
{
	public PropertyConcreteColumnValue()
	{
		super();
	}

	public PropertyConcreteColumnValue(Object value)
	{
		super(value);
	}
}
