/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;

/**
 * 正则表达式。
 * 
 * @author datagear@163.com
 *
 */
public class Regex extends ValueFeature<String> implements PropertyFeature
{
	public Regex()
	{
		super();
	}

	public Regex(String value)
	{
		super(value);
	}
}
