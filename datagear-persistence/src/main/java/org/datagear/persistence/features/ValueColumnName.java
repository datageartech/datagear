/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
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
public class ValueColumnName extends ValueFeature<String> implements PropertyFeature, PersistenceFeature
{
	public ValueColumnName()
	{
		super();
	}

	public ValueColumnName(String defaultValue)
	{
		super(defaultValue);
	}
}
