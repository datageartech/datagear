/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.model.support.MU;
import org.datagear.persistence.PersistenceFeature;

/**
 * 列名称。
 * <p>
 * 它可用于如下场景：
 * </p>
 * <ul>
 * <li>单元（{@linkplain MU#isSingleProperty(Property)}）、基本（
 * {@linkplain MU#isPrimitiveProperty(Property)}）属性：
 * <p>
 * 用于定义模型表内属性列名称。
 * </p>
 * </li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class ColumnName extends ValueFeature<String> implements PropertyFeature, PersistenceFeature
{
	public ColumnName()
	{
		super();
	}

	public ColumnName(String value)
	{
		super(value);
	}
}
