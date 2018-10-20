/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端外键属性名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelKeyPropertyName extends MapFeature<Integer, String[]> implements PropertyFeature, PersistenceFeature
{
	public ModelKeyPropertyName()
	{
		super();
	}

	public ModelKeyPropertyName(String[] defaultValue)
	{
		super(defaultValue);
	}

	public ModelKeyPropertyName(Map<Integer, String[]> mapValues)
	{
		super(mapValues);
	}

	public ModelKeyPropertyName(String[] defaultValue, Map<Integer, String[]> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
