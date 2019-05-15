/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 用于模型端的外键删除策略。
 * 
 * @author datagear@163.com
 *
 */
public class ModelKeyDeleteRule extends ValueFeature<KeyRule> implements PropertyFeature, PersistenceFeature
{
	public ModelKeyDeleteRule()
	{
		super();
	}

	public ModelKeyDeleteRule(KeyRule value)
	{
		super(value);
	}
}
