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

package org.datagear.persistence;

import java.util.List;

/**
 * 分页数据。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class PagingData<T>
{
	/** 默认显示的页码链接数 */
	public static final int DEFAULT_PAGE_SPAN_NUM = 5;

	/** 总记录数 */
	private long total = 0;

	/** 当前页数据 */
	private List<T> items;

	/** 总页数 */
	private int pages = 0;

	/** 分页信息 */
	private Paging paging = new Paging();

	public PagingData()
	{
		this(1, 0);
	}

	public PagingData(int page, long total)
	{
		this(page, total, Paging.DEFAULT_PAGE_SIZE);
	}

	public PagingData(int page, long total, int pageSize)
	{
		this.total = total;

		this.pages = (int) (this.total / pageSize);
		if (this.total % pageSize > 0)
			this.pages += 1;

		if (page > this.pages)
			page = this.pages;
		if (page < 1)
			page = 1;

		this.paging.setPage(page);
		this.paging.setPageSize(pageSize);
	}

	public long getTotal()
	{
		return total;
	}

	public void setTotal(long total)
	{
		this.total = total;
	}

	public List<T> getItems()
	{
		return items;
	}

	public void setItems(List<T> items)
	{
		this.items = items;
	}

	public int getPages()
	{
		return pages;
	}

	public void setPages(int pages)
	{
		this.pages = pages;
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

	/**
	 * 获取当前页在整个记录中的开始位置，以{@code 0}开始计数。
	 * 
	 * @return
	 */
	public int getStartIndex()
	{
		int pageIndex = getPageSize() * (getPage() - 1);
		if (pageIndex < 0)
			pageIndex = 0;

		return pageIndex;
	}

	/**
	 * 获取当前页在整个记录中的开始行号，以{@code 1}开始计数。
	 * 
	 * @return
	 */
	public int getStartRow()
	{
		return getStartIndex() + 1;
	}

	/**
	 * 获取当前页在整个记录中的结束位置，以{@code 0}开始计数。
	 * 
	 * @return
	 */
	public int getEndIndex()
	{
		int startIndex = getStartIndex();
		int endIndex = startIndex + getPageSize();

		if (endIndex > (int) this.total)
			endIndex = (int) this.total;

		return endIndex;
	}

	/**
	 * 获取当前页在整个记录中的结束行号，以{@code 1}开始计数。
	 * 
	 * @return
	 */
	public int getEndRow()
	{
		return getEndIndex() + 1;
	}
}
