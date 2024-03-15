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

package org.datagear.util.cache;

import java.io.Serializable;

/**
 * 通用缓存KEY。
 * <p>
 * 通用缓存KEY应：
 * </p>
 * <p>
 * 实现{@linkplain Serializable}，以支持序列化/反序列化缓存KEY。
 * </p>
 * <p>
 * 实现{@linkplain #hashCode()}、{@linkplain #equals(Object)}，以支持缓存KEY比较。
 * </p>
 * <p>
 * 实现{@linkplain #toString()}，以支持转换为字符串缓存KEY。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface CommonCacheKey extends Serializable
{
	/**
	 * 获取缓存KEY的hash值。
	 * 
	 * @return
	 */
	@Override
	int hashCode();

	/**
	 * 比较缓存KEY。
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * 获取缓存KEY字符串格式。
	 * 
	 * @return
	 */
	@Override
	String toString();
}
