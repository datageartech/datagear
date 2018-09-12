/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于属性端的外键删除策略。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyKeyDeleteRule extends MapFeature<Integer, KeyRule>
		implements PropertyFeature, PersistenceFeature
{
	public PropertyKeyDeleteRule()
	{
		super();
	}

	public PropertyKeyDeleteRule(KeyRule value)
	{
		super(value);
	}

	public PropertyKeyDeleteRule(Map<Integer, KeyRule> mapValues)
	{
		super(mapValues);
	}

	public PropertyKeyDeleteRule(KeyRule value, Map<Integer, KeyRule> mapValues)
	{
		super(value, mapValues);
	}
}
