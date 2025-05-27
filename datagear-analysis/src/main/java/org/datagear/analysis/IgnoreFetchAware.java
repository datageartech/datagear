/*
 * Copyright 2018-present datagear.tech
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

/**
 * 忽略获取结果相关类。
 * 
 * @author datagear@163.com
 *
 */
public interface IgnoreFetchAware
{
	/**
	 * 是否忽略获取结果的默认值，应为：{@code false}，以兼容旧版逻辑
	 */
	boolean DEFAULT = false;

	/**
	 * 是否忽略获取结果
	 * 
	 * @return
	 */
	boolean isIgnoreFetch();

	/**
	 * 设置是否忽略获取结果
	 * 
	 * @param ignoreFetch
	 */
	void setIgnoreFetch(boolean ignoreFetch);
}
