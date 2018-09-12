/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端外键列名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyKeyColumnName extends MapFeature<Integer, String[]> implements PropertyFeature, PersistenceFeature
{
	public PropertyKeyColumnName()
	{
		super();
	}

	public PropertyKeyColumnName(String[] defaultValue)
	{
		super(defaultValue);
	}

	public PropertyKeyColumnName(Map<Integer, String[]> mapValues)
	{
		super(mapValues);
	}

	public PropertyKeyColumnName(String[] defaultValue, Map<Integer, String[]> mapValues)
	{
		super(defaultValue, mapValues);
	}
}