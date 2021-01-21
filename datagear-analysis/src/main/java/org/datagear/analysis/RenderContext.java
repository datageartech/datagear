/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 渲染上下文。
 * <p>
 * 此类用于定义图表、看板UI渲染上下文。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface RenderContext
{
	String PROPERTY_ATTRIBUTES = "attributes";

	/**
	 * 获取属性。
	 * 
	 * @param <T>
	 * @param name
	 * @return
	 */
	<T> T getAttribute(String name);

	/**
	 * 设置属性。
	 * 
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, Object value);

	/**
	 * 移除属性。
	 * 
	 * @param <T>
	 * @param name
	 * @return 已移除的属性值或者{@code null}
	 */
	<T> T removeAttribute(String name);

	/**
	 * 是否有指定属性。
	 * 
	 * @param name
	 * @return
	 */
	boolean hasAttribute(String name);

	/**
	 * 获取所有属性。
	 * 
	 * @return
	 */
	Map<String, ?> getAttributes();
}
