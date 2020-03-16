/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.model;

import java.util.Map;

/**
 * 特性对象。
 * <p>
 * 注意：对于特性标识为{@linkplain Class}的情况，实现类应该将其转换为{@linkplain Class#getSimpleName()}存储，
 * 这样可以避免JSON输出时的标识转换处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface Featured
{
	/**
	 * 是否至少包含一个特性。
	 * 
	 * @return
	 */
	boolean hasFeature();

	/**
	 * 是否包含指定标识的特性。
	 * 
	 * @param key
	 * @return
	 */
	boolean hasFeature(Object key);

	/**
	 * 获取指定标识的特性。
	 * <p>
	 * 如果{@linkplain #hasFeature(Object)}为{@code false}，此方法将返回{@code null} 。
	 * </p>
	 * 
	 * @param key
	 * @return
	 */
	<T> T getFeature(Object key);

	/**
	 * 设置特性。
	 * 
	 * @param key
	 */
	void setFeature(Object key);

	/**
	 * 设置特性。
	 * 
	 * @param key
	 * @param value
	 */
	void setFeature(Object key, Object value);

	/**
	 * 删除特性。
	 * 
	 * @param key
	 * @return
	 */
	<T> T removeFeature(Object key);

	/**
	 * 获取特性映射表。
	 * <p>
	 * 如果{@linkplain #hasFeature()}为{@code true}，此方法将返回一个非空映射表。
	 * </p>
	 * 
	 * @return
	 */
	Map<?, ?> getFeatures();
}
