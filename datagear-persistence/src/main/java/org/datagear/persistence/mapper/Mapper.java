/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.mapper;

import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.MappedBy;

/**
 * 映射。
 * 
 * @author datagear@163.com
 *
 */
public interface Mapper
{
	/**
	 * 是否有{@linkplain MappedBy}特性。
	 * 
	 * @return
	 */
	boolean isMappedBySource();

	/**
	 * 当{@linkplain #isMappedBySource()}为}{@code true}时，获取映射目标属性名称。
	 * 
	 * @return
	 */
	String getMappedByTarget();

	/**
	 * 是否是{@linkplain MappedBy}的目标。
	 * 
	 * @return
	 */
	boolean isMappedByTarget();

	/**
	 * 当{@linkplain #isMappedByTarget()}为}{@code true}时，获取映射源属性名称。
	 * 
	 * @return
	 */
	String getMappedBySource();

	/**
	 * 是否有模型端具体模型列名称。
	 * 
	 * @return
	 */
	boolean hasModelConcreteColumn();

	/**
	 * {@linkplain #hasModelConcreteColumn()}为{@code true}时，获取模型端具体模型列名称。
	 * 
	 * @return
	 */
	String getModelConcreteColumnName();

	/**
	 * {@linkplain #hasModelConcreteColumn()}为{@code true}时，获取模型端具体模型列值。
	 * 
	 * @return
	 */
	Object getModelConcreteColumnValue();

	/**
	 * 是否有属性端具体模型列名称。
	 * 
	 * @return
	 */
	boolean hasPropertyConcreteColumn();

	/**
	 * {@linkplain #hasPropertyConcreteColumn()}为{@code true}时，获取属性端具体模型列名称。
	 * 
	 * @return
	 */
	String getPropertyConcreteColumnName();

	/**
	 * {@linkplain #hasPropertyConcreteColumn()}为{@code true}时，获取属性端具体模型列值。
	 * 
	 * @return
	 */
	Object getPropertyConcreteColumnValue();

	/**
	 * 获取属性端键更新策略。
	 * <p>
	 * 在某些映射中，此返回值可能为{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	KeyRule getPropertyKeyUpdateRule();

	/**
	 * 获取属性端键删除策略。
	 * <p>
	 * 在某些映射中，此返回值可能为{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	KeyRule getPropertyKeyDeleteRule();
}
