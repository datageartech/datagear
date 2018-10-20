/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端排序值列名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToMany}、{@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelOrderColumnName extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public ModelOrderColumnName()
	{
		super();
	}

	public ModelOrderColumnName(String defaultValue)
	{
		super(defaultValue);
	}

	public ModelOrderColumnName(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public ModelOrderColumnName(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
