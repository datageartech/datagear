/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.features;

import java.util.Map;

import org.datagear.model.MapFeature;
import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;

/**
 * 模型端具体模型列名。
 * <p>
 * 如果列不能直接存储具体模型名称字符串，可以使用{@linkplain ModelConcreteColumnValue}自定义。
 * </p>
 * <p>
 * 具体使用场景参考{@linkplain OneToOne}、{@linkplain OneToMany}、{@linkplain ManyToOne}、
 * {@linkplain ManyToMany}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ModelConcreteColumnName extends MapFeature<Integer, String> implements PropertyFeature, PersistenceFeature
{
	public ModelConcreteColumnName()
	{
		super();
	}

	public ModelConcreteColumnName(String defaultValue)
	{
		super(defaultValue);
	}

	public ModelConcreteColumnName(Map<Integer, String> mapValues)
	{
		super(mapValues);
	}

	public ModelConcreteColumnName(String defaultValue, Map<Integer, String> mapValues)
	{
		super(defaultValue, mapValues);
	}
}
