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

package org.datagear.util;

/**
 * 上次修改时间服务。
 * 
 * @author datagear@163.com
 *
 */
public interface LastModifiedService
{
	/**
	 * 未设置时的默认值。
	 */
	long LAST_MODIFIED_UNSET = -1;

	/**
	 * 供外部使用的初始值。
	 */
	long LAST_MODIFIED_INIT = -99;

	/**
	 * 获取上次修改时间。
	 * 
	 * @param name
	 * @return 返回{@linkplain #LAST_MODIFIED_UNSET}表示从未调用过{@linkplain #setLastModified(String, long)}设置。
	 */
	long getLastModified(String name);

	/**
	 * 设置上次修改时间。
	 * 
	 * @param name
	 * @param lastModified
	 */
	void setLastModified(String name, long lastModified);

	/**
	 * 将上次修改时间设置为当前时间。
	 * 
	 * @param name
	 * @return
	 */
	long setLastModifiedNow(String name);

	/**
	 * 是否上次修改时间有变。
	 * 
	 * @param lastModified
	 * @return
	 */
	boolean isModified(String name, long lastModified);
}
