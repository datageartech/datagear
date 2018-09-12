/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model.features;

import org.datagear.model.Label;
import org.datagear.model.ModelFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;

/**
 * 名称标签。
 * 
 * @author datagear@163.com
 *
 */
public class NameLabel extends ValueFeature<Label> implements ModelFeature, PropertyFeature
{
	public NameLabel()
	{
		super();
	}

	public NameLabel(Label value)
	{
		super(value);
	}
}
