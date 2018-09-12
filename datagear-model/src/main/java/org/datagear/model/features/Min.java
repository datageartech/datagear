/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;

/**
 * 最小值。
 * 
 * @author datagear@163.com
 *
 */
public class Min extends ValueFeature<Number> implements PropertyFeature
{
	public Min()
	{
		super();
	}

	public Min(Number value)
	{
		super(value);
	}
}
