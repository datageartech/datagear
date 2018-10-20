/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model.support;

import java.util.Map;

import org.datagear.model.Model;

/**
 * 基本模型源。
 * <p>
 * 基本模型源用于获取不可分割的，没有业务含义的基本模型，通常是基本类型对应的模型。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface PrimitiveModelSource
{
	/**
	 * 是否包含指定类型的基本模型。
	 * 
	 * @param type
	 * @return
	 */
	boolean contains(Class<?> type);

	/**
	 * 获取指定类型的基本模型。
	 * 
	 * @param type
	 * @return 基本模型，没有则返回{@code null}。
	 */
	Model get(Class<?> type);

	/**
	 * 转换为映射表。
	 * 
	 * @return
	 */
	Map<Class<?>, Model> toMap();
}
