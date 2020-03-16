/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于属性端的外键删除策略。
 * 
 * @author datagear@163.com
 *
 */
public class PropertyKeyDeleteRule extends ValueFeature<KeyRule> implements PropertyFeature, PersistenceFeature
{
	public PropertyKeyDeleteRule()
	{
		super();
	}

	public PropertyKeyDeleteRule(KeyRule value)
	{
		super(value);
	}
}
