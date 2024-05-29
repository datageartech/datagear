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

package org.datagear.util.dirquery;

import org.datagear.util.query.Order;
import org.datagear.util.query.Paging;

/**
 * 目录分页查询。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryPagingQuery extends DirectoryQuery
{
	private static final long serialVersionUID = 1L;

	/** 分页信息 */
	private Paging paging = new Paging();

	public DirectoryPagingQuery()
	{
		super();
	}

	public DirectoryPagingQuery(int page, String keyword)
	{
		super(keyword);
		this.paging.setPage(page);
	}

	public DirectoryPagingQuery(int page, String keyword, Order order)
	{
		super(keyword, order);
		this.paging.setPage(page);
	}

	public DirectoryPagingQuery(int page, String keyword, Order order, String queryRange)
	{
		super(keyword, order, queryRange);
		this.paging.setPage(page);
	}

	public int getPage()
	{
		return this.paging.getPage();
	}

	public void setPage(int page)
	{
		this.paging.setPage(page);
	}

	public int getPageSize()
	{
		return this.paging.getPageSize();
	}

	public void setPageSize(int pageSize)
	{
		this.paging.setPageSize(pageSize);
	}
}
