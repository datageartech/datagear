/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * JDBC类型。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcType extends MapFeature<Integer, Integer> implements PropertyFeature, PersistenceFeature
{
	public JdbcType()
	{
		super();
	}

	public JdbcType(int value)
	{
		super(value);
	}

	public JdbcType(Map<Integer, Integer> mapValues)
	{
		super(mapValues);
	}

	public JdbcType(int value, Map<Integer, Integer> mapValues)
	{
		super(value, mapValues);
	}
}
