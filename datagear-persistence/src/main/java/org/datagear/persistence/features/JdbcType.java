/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * JDBC类型。
 * 
 * @author datagear@163.com
 *
 */
public class JdbcType extends ValueFeature<Integer> implements PropertyFeature, PersistenceFeature
{
	public JdbcType()
	{
		super();
	}

	public JdbcType(int value)
	{
		super(value);
	}
}
