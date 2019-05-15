/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端外键属性名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyKeyPropertyName extends ValueFeature<String[]> implements PropertyFeature, PersistenceFeature
{
	public PropertyKeyPropertyName()
	{
		super();
	}

	public PropertyKeyPropertyName(String[] value)
	{
		super(value);
	}
}