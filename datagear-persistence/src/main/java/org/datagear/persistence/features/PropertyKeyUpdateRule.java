/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于属性端的外键更新策略。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyKeyUpdateRule extends MapFeature<Integer, KeyRule>
		implements PropertyFeature, PersistenceFeature
{
	public PropertyKeyUpdateRule()
	{
		super();
	}

	public PropertyKeyUpdateRule(KeyRule value)
	{
		super(value);
	}

	public PropertyKeyUpdateRule(Map<Integer, KeyRule> mapValues)
	{
		super(mapValues);
	}

	public PropertyKeyUpdateRule(KeyRule value, Map<Integer, KeyRule> mapValues)
	{
		super(value, mapValues);
	}
}
