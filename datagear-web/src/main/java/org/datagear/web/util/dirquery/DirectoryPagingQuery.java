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

package org.datagear.web.util.dirquery;

import org.datagear.persistence.PagingQuery;

/**
 * 目录分页查询。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryPagingQuery extends PagingQuery
{
	private static final long serialVersionUID = 1L;

	/**
	 * 查询范围：本级
	 */
	public static final String QUERY_RANGE_CHILDREN = "children";

	/**
	 * 查询范围：全级
	 */
	public static final String QUERY_RANGE_DESCENDANT = "descendant";

	/**
	 * 查询范围。
	 */
	private String queryRange = QUERY_RANGE_CHILDREN;

	public DirectoryPagingQuery()
	{
		super();
	}

	public DirectoryPagingQuery(int page)
	{
		super(page);
	}

	public DirectoryPagingQuery(int page, int pageSize)
	{
		super(page, pageSize);
	}

	public DirectoryPagingQuery(int page, int pageSize, String keyword)
	{
		super(page, pageSize, keyword);
	}

	public DirectoryPagingQuery(int page, int pageSize, String keyword, String condition)
	{
		super(page, pageSize, keyword, condition);
	}

	public String getQueryRange()
	{
		return queryRange;
	}

	public void setQueryRange(String queryRange)
	{
		this.queryRange = queryRange;
	}
}
