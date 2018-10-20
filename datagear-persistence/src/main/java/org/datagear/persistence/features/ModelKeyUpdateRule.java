/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于模型端的外键更新策略。
 * 
 * @author datagear@163.com
 *
 */
public class ModelKeyUpdateRule extends MapFeature<Integer, KeyRule>
		implements PropertyFeature, PersistenceFeature
{
	public ModelKeyUpdateRule()
	{
		super();
	}

	public ModelKeyUpdateRule(KeyRule value)
	{
		super(value);
	}

	public ModelKeyUpdateRule(Map<Integer, KeyRule> mapValues)
	{
		super(mapValues);
	}

	public ModelKeyUpdateRule(KeyRule value, Map<Integer, KeyRule> mapValues)
	{
		super(value, mapValues);
	}
}
