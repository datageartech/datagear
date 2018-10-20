/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端具体模型列值。
 * <p>
 * 它可用于{@linkplain PropertyConcreteColumnName}属性，用于定义属性具体模型的列值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyConcreteColumnValue extends MapFeature<Integer, Object>
		implements PropertyFeature, PersistenceFeature
{
	public PropertyConcreteColumnValue()
	{
		super();
	}

	public PropertyConcreteColumnValue(Object defaultValue)
	{
		super(defaultValue);
	}

	public PropertyConcreteColumnValue(Map<Integer, Object> mapValues)
	{
		super(mapValues);
	}

	public PropertyConcreteColumnValue(Object defaultValue, Map<Integer, Object> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
