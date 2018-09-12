/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 双向关联关系中关系持有属性名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MappedBy extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public MappedBy()
	{
		super();
	}

	public MappedBy(String defaultValue)
	{
		super(defaultValue);
	}

	public MappedBy(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public MappedBy(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
