/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.ModelFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.model.support.MU;
import org.datagear.persistence.PersistenceFeature;

/**
 * 表名称。
 * <p>
 * 它可用于如下场景：
 * </p>
 * <ul>
 * <li>模型
 * <p>
 * 用于定义模型表名称
 * </p>
 * </li>
 * <li>
 * <p>
 * {@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}属性：
 * <p>
 * 具体参考相关类说明。
 * </p>
 * </p>
 * </li>
 * <li>单元（ {@linkplain MU#isSingleProperty(Property)}）、复合（
 * {@linkplain MU#isCompositeProperty(Property)}）、值（
 * {@linkplain MU#isValueProperty(Property)}）属性：
 * <p>
 * 用于定义属性值表名称。
 * </p>
 * </li>
 * <li>多元（ {@linkplain MU#isMultipleProperty(Property)}）、值（
 * {@linkplain MU#isValueProperty(Property)}）属性：
 * <p>
 * 用于定义属性值表名称。
 * </p>
 * </li>
 * </ul>
 * 
 * @author datagear@163.com
 *
 */
public class TableName extends MapFeature<Integer, String> implements ModelFeature, PropertyFeature, PersistenceFeature
{
	public TableName()
	{
		super();
	}

	public TableName(String defaultValue)
	{
		super(defaultValue);
	}

	public TableName(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public TableName(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
