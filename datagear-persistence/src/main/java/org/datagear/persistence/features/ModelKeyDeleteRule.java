/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于模型端的外键删除策略。
 * 
 * @author datagear@163.com
 *
 */
public class ModelKeyDeleteRule extends MapFeature<Integer, KeyRule>
		implements PropertyFeature, PersistenceFeature
{
	public ModelKeyDeleteRule()
	{
		super();
	}

	public ModelKeyDeleteRule(KeyRule value)
	{
		super(value);
	}

	public ModelKeyDeleteRule(Map<Integer, KeyRule> mapValues)
	{
		super(mapValues);
	}

	public ModelKeyDeleteRule(KeyRule value, Map<Integer, KeyRule> mapValues)
	{
		super(value, mapValues);
	}
}
