/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 关联关系存储位置。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class RelationPoint extends MapFeature<Integer, PointType> implements PropertyFeature, PersistenceFeature
{
	public RelationPoint()
	{
		super();
	}

	public RelationPoint(PointType defaultValue)
	{
		super(defaultValue);
	}

	public RelationPoint(Map<Integer, PointType> mapValues)
	{
		super(mapValues);
	}

	public RelationPoint(PointType defaultValue, Map<Integer, PointType> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
