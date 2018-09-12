/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.model;

import java.util.Collection;
import java.util.Map;

/**
 * 模型管理器。
 * <p>
 * 它不应该存储重复名称（{@link Model#getName()}）的{@link Model}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ModelManager
{
	/**
	 * 获取指定名称的{@linkplain Model 模型}。
	 * 
	 * @param name
	 * @return
	 */
	Model get(String name);

	/**
	 * 存放指定{@linkplain Model}。
	 * 
	 * @param model
	 * @return 如果之前有同名的{@linkplain Model}，则返回之；否则，返回{@code null}。
	 */
	Model put(Model model);

	/**
	 * 存放{@linkplain Model}数组。
	 * 
	 * @param models
	 */
	void putAll(Model[] models);

	/**
	 * 存放{@linkplain Model}集合。
	 * 
	 * @param models
	 */
	void putAll(Collection<? extends Model> models);

	/**
	 * 删除指定名称的{@linkplain Model}。
	 * 
	 * @param name
	 * @return 如果已存在同名的{@linkplain Model}，则删除并返回之；否则，直接返回{@code null}。
	 */
	Model remove(String name);

	/**
	 * 清除。
	 */
	void clear();

	/**
	 * 是否包含指定名称的{@linkplain Model}。
	 * 
	 * @param name
	 * @return
	 */
	boolean contains(String name);

	/**
	 * 获取{@linkplain Model}个数。
	 * 
	 * @return
	 */
	int size();

	/**
	 * 是否为空。
	 * 
	 * @return
	 */
	boolean isEmpty();

	/**
	 * 获取一个包含所有{@linkplain Model}集合。
	 * 
	 * @return
	 */
	Collection<Model> toCollection();

	/**
	 * 获取一个包含所有{@linkplain Model}的映射表，关键字为{@linkplain Model#getName()}。
	 * 
	 * @return
	 */
	Map<String, Model> toMap();
}
