/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.features;

import org.datagear.model.Label;
import org.datagear.model.ModelFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;

/**
 * 描述标签。
 * 
 * @author datagear@163.com
 *
 */
public class DescLabel extends ValueFeature<Label> implements ModelFeature, PropertyFeature
{
	public DescLabel()
	{
		super();
	}

	public DescLabel(Label value)
	{
		super(value);
	}
}
