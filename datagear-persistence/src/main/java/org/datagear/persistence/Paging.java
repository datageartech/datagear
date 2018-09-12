/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence;

import java.io.Serializable;

/**
 * 分页信息。
 *
 * @author datagear@163.com
 * @createDate 2013-2-4
 *
 */
public class Paging implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 默认每页显示记录数 */
	public static final int DEFAULT_PAGE_SIZE = 20;

	/** 页码 */
	private int page = 1;

	/** 每页记录数 */
	private int pageSize = DEFAULT_PAGE_SIZE;

	public Paging()
	{
		super();
	}

	public Paging(int page)
	{
		super();
		this.page = page;
	}

	public Paging(int page, int pageSize)
	{
		super();
		this.page = page;
		this.pageSize = pageSize;
	}

	public int getPage()
	{
		return page;
	}

	public void setPage(int page)
	{
		this.page = page;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [page=" + page + ", pageSize=" + pageSize + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + page;
		result = prime * result + pageSize;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Paging other = (Paging) obj;
		if (page != other.page)
			return false;
		if (pageSize != other.pageSize)
			return false;
		return true;
	}
}
