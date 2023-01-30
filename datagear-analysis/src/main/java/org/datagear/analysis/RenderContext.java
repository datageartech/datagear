/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	
	/**
	 * 设置多个属性。
	 * 
	 * @param attrs
	 */
	void putAttributes(Map<String, ?> attrs);
	
	/**
	 * 设置多个属性。
	 * 
	 * @param renderContext
	 */
	void putAttributes(RenderContext renderContext);
	
}
