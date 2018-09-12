/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.model.support.MU;
import org.datagear.persistence.PersistenceFeature;

/**
 * 值列名称。
 * <p>
 * 它可用于如下场景：
 * </p>
 * <ul>
 * <li>多元（ {@linkplain MU#isMultipleProperty(Property)}）、基本（
 * {@linkplain MU#isPrimitiveProperty(Property)}）属性：
 * <p>
 * 定义属性表内的属性值列名称。
 * </p>
 * </li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class ValueColumnName extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public ValueColumnName()
	{
		super();
	}

	public ValueColumnName(String defaultValue)
	{
		super(defaultValue);
	}

	public ValueColumnName(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public ValueColumnName(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
