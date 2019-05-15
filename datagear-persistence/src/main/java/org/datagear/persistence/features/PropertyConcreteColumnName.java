/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.features;

import org.datagear.model.PropertyFeature;
import org.datagear.model.ValueFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 属性端具象列名。
 * <p>
 * 当表中混合存储其他模型的属性数据时，可以使用此类标识区分。
 * </p>
 * <p>
 * 如果列不能直接存储属性模型名称字符串，可以使用{@linkplain PropertyConcreteColumnValue}自定义。
 * </p>
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PropertyConcreteColumnName extends ValueFeature<String> implements PropertyFeature, PersistenceFeature
{
	public PropertyConcreteColumnName()
	{
		super();
	}

	public PropertyConcreteColumnName(String value)
	{
		super(value);
	}
}
