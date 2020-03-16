/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.mapper;

import org.datagear.model.PropertyFeature;
import org.datagear.persistence.PersistenceFeature;
import org.datagear.persistence.features.KeyRule;
import org.datagear.persistence.features.MappedBy;

/**
 * 持久化映射接口类。
 * <p>
 * 持久化处理时，会大量使用此接口，为了提高效率和便于使用，将此接口设计为{@linkplain PersistenceFeature}特性。
 * </p>
 * <p>
 * 注意：此特性的逻辑依赖于{@linkplain org.datagear.persistence.features} 包下的特性，因此在调用
 * {@linkplain MapperResolver}解析时，要确保依赖特性都已设置完成。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface Mapper extends PropertyFeature, PersistenceFeature
{
	/**
	 * 获取{@linkplain RelationType}。
	 * 
	 * @return
	 */
	RelationType getRelationType();

	/**
	 * 是否是一对一关联。
	 * 
	 * @return
	 */
	boolean isOneToOne();

	/**
	 * 是否是一对多关联。
	 * 
	 * @return
	 */
	boolean isOneToMany();

	/**
	 * 是否是多对一关联。
	 * 
	 * @return
	 */
	boolean isManyToOne();

	/**
	 * 是否是多对多关联。
	 * 
	 * @return
	 */
	boolean isManyToMany();

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
	 * 是否有模型端具象列名称。
	 * 
	 * @return
	 */
	boolean hasModelConcreteColumn();

	/**
	 * {@linkplain #hasModelConcreteColumn()}为{@code true}时，获取模型端具象列名称。
	 * 
	 * @return
	 */
	String getModelConcreteColumnName();

	/**
	 * {@linkplain #hasModelConcreteColumn()}为{@code true}时，获取模型端具象列值。
	 * 
	 * @return
	 */
	Object getModelConcreteColumnValue();

	/**
	 * 是否有属性端具象列名称。
	 * 
	 * @return
	 */
	boolean hasPropertyConcreteColumn();

	/**
	 * {@linkplain #hasPropertyConcreteColumn()}为{@code true}时，获取属性端具象列名称。
	 * 
	 * @return
	 */
	String getPropertyConcreteColumnName();

	/**
	 * {@linkplain #hasPropertyConcreteColumn()}为{@code true}时，获取属性端具象列值。
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
