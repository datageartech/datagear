/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端具体模型列值。
 * <p>
 * 它可用于{@linkplain ModelConcreteColumnName}属性，用于定义具体模型的列值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelConcreteColumnValue extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public ModelConcreteColumnValue()
	{
		super();
	}

	public ModelConcreteColumnValue(String defaultValue)
	{
		super(defaultValue);
	}

	public ModelConcreteColumnValue(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public ModelConcreteColumnValue(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
