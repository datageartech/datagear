/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端排序值列名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToMany}、{@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelOrderColumnName extends ValueFeature<String> implements PropertyFeature, PersistenceFeature
{
	public ModelOrderColumnName()
	{
		super();
	}

	public ModelOrderColumnName(String defaultValue)
	{
		super(defaultValue);
	}
}
