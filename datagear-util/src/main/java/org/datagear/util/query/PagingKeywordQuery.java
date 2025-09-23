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

import java.io.Serializable;

/**
 * 分页关键字查询。
 * 
 * @author datagear@163.com
 *
 */
public class PagingKeywordQuery extends KeywordQuery implements PagingAware, Serializable
{
	private static final long serialVersionUID = 1L;

	private Paging paging = new Paging();

	public PagingKeywordQuery()
	{
		super();
	}

	public PagingKeywordQuery(String keyword)
	{
		super(keyword);
	}

	@Override
	public int getPage()
	{
		return this.paging.getPage();
	}

	@Override
	public void setPage(int page)
	{
		this.paging.setPage(page);
	}

	@Override
	public int getPageSize()
	{
		return this.paging.getPageSize();
	}

	@Override
	public void setPageSize(int pageSize)
	{
		this.paging.setPageSize(pageSize);
	}
}
