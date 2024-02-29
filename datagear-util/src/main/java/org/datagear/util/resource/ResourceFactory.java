/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.util.resource;

/**
 * 资源工厂。
 * <p>
 * 此类用于获取、释放资源。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface ResourceFactory<T>
{
	/**
	 * 获取资源。
	 * 
	 * @return
	 * @throws Exception
	 */
	T get() throws Exception;

	/**
	 * 释放资源。
	 * 
	 * @param resource
	 * @throws Exception
	 */
	void release(T resource) throws Exception;
}
