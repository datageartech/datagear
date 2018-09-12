/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端排序值列名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToMany}、{@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyOrderColumnName extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public PropertyOrderColumnName()
	{
		super();
	}

	public PropertyOrderColumnName(String defaultValue)
	{
		super(defaultValue);
	}

	public PropertyOrderColumnName(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public PropertyOrderColumnName(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
