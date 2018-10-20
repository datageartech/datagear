/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

import java.util.Collection;

/**
 * 属性。
 * <p>
 * <i>属性</i>描述模型特定属性的元信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface Property extends Featured
{
	/**
	 * 获取属性名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 是否是<i>抽象属性</i>。
	 * <p>
	 * <i>抽象属性</i>有多个属性模型（由{@linkplain #getModels()}获取），<i>具体属性</i>（非
	 * <i>抽象属性</i>）仅有一个属性模型（{@linkplain #getModel()}）。
	 * </p>
	 * 
	 * @return
	 */
	boolean isAbstracted();

	/**
	 * 获取<i>具体属性</i>的{@linkplain Model 模型}。
	 * <p>
	 * 如果{@linkplain #isArray()}或{@linkplain #isCollection()}为{@code true}
	 * ，则此方法返回的是元素{@linkplain Model 模型}。
	 * </p>
	 * 
	 * @return
	 */
	Model getModel();

	/**
	 * 获取<i>抽象属性</i>的{@linkplain Model 模型}数组。
	 * <p>
	 * 如果{@linkplain #isArray()}或{@linkplain #isCollection()}为{@code true}
	 * ，则此方法返回的是元素{@linkplain Model 模型}。
	 * </p>
	 * 
	 * @return
	 */
	Model[] getModels();

	/**
	 * 获取<i>抽象属性</i>的{@linkplain Model 模型}数目。
	 * 
	 * @return
	 */
	int getModelCount();

	/**
	 * 获取<i>抽象属性</i>的指定位置{@linkplain Model 模型}。
	 * 
	 * @param index
	 * @return
	 */
	Model getModel(int index);

	/**
	 * 获取<i>抽象属性</i>的指定名称{@linkplain Model 模型}。
	 * <p>
	 * 注意：此方法限定<i>抽象属性</i>不能包含具有相同{@linkplain Model#getName() 名称}的
	 * {@linkplain Model 模型}。
	 * </p>
	 * 
	 * @param modelName
	 * @return
	 */
	Model getModel(String modelName);

	/**
	 * 是否是数组属性。
	 * 
	 * @return
	 */
	boolean isArray();

	/**
	 * 是否是集合属性。
	 * 
	 * @return
	 */
	boolean isCollection();

	/**
	 * 获取集合类型。
	 * <p>
	 * 如果{@linkplain #isCollection()}为{@code true}，此方法不允许返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Collection> getCollectionType();

	/**
	 * 获取默认值。
	 * <p>
	 * 如果没有默认值，此方法应该返回{@code null}。
	 * </p>
	 * 
	 * @return
	 */
	Object getDefaultValue();

	/**
	 * 获取指定模型实例对象的此属性值。
	 * 
	 * @param obj
	 *            模型对象。
	 * @return
	 * @throws PropertyAccessException
	 */
	Object get(Object obj) throws PropertyAccessException;

	/**
	 * 设置指定模型实例对象的此属性值。
	 * 
	 * @param obj
	 *            模型对象。
	 * @param value
	 *            属性值。
	 * @throws PropertyAccessException
	 */
	void set(Object obj, Object value) throws PropertyAccessException;
}
