/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 双向关联关系中关系持有属性名称。
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MappedBy extends ValueFeature<String> implements PropertyFeature, PersistenceFeature
{
	public MappedBy()
	{
		super();
	}

	public MappedBy(String value)
	{
		super(value);
	}
}
