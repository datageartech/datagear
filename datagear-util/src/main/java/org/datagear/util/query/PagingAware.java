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

package org.datagear.util.query;

/**
 * 分页相关类。
 * 
 * @author datagear@163.com
 *
 */
public interface PagingAware
{
	/**
	 * 获取页码，应以{@code 1}开始。
	 * 
	 * @return
	 */
	int getPage();

	/**
	 * 设置页码，应以{@code 1}开始。
	 * 
	 * @param page
	 */
	void setPage(int page);

	/**
	 * 获取页大小。
	 * 
	 * @return
	 */
	int getPageSize();

	/**
	 * 设置页大小，应大于{@code 0}。
	 * 
	 * @param pageSize
	 */
	void setPageSize(int pageSize);
}
