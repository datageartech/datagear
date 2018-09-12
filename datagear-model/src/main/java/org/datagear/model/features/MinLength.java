/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;

/**
 * 最小长度。
 * 
 * @author datagear@163.com
 *
 */
public class MinLength extends ValueFeature<Integer> implements PropertyFeature
{
	public MinLength()
	{
		super();
	}

	public MinLength(Integer value)
	{
		super(value);
	}
}
