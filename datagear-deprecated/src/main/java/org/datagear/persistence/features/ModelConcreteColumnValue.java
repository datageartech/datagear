/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端具象列值。
 * <p>
 * 它可结合{@linkplain ModelConcreteColumnName}属性，用于自定义模型端具象列值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelConcreteColumnValue extends ValueFeature<Object> implements PropertyFeature, PersistenceFeature
{
	public ModelConcreteColumnValue()
	{
		super();
	}

	public ModelConcreteColumnValue(Object value)
	{
		super(value);
	}
}
