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

package org.datagear.management.util;

import java.util.Arrays;

import org.datagear.util.query.Paging;
import org.datagear.util.query.PagingAware;

/**
 * 分页查询。
 *
 * @author datagear@163.com
 *
 */
public class PagingQuery extends Query implements PagingAware
{
	private static final long serialVersionUID = 1L;

	/** 分页信息 */
	private Paging paging = new Paging();

	public PagingQuery()
	{
		super();
	}

	public PagingQuery(int page)
	{
		super();
		this.paging.setPage(page);
	}

	public PagingQuery(int page, int pageSize)
	{
		super();
		this.paging.setPage(page);
		this.paging.setPageSize(pageSize);
	}

	public PagingQuery(int page, int pageSize, String keyword)
	{
		super(keyword);
		this.paging.setPage(page);
		this.paging.setPageSize(pageSize);
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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [page=" + getPage() + ", pageSize=" + getPageSize() + ", keyword="
				+ getKeyword() + ", orders=" + Arrays.toString(getOrders()) + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((paging == null) ? 0 : paging.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PagingQuery other = (PagingQuery) obj;
		if (paging == null)
		{
			if (other.paging != null)
				return false;
		}
		else if (!paging.equals(other.paging))
			return false;
		return true;
	}
}
