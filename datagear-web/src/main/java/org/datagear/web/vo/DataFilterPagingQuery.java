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

package org.datagear.web.vo;

import org.datagear.persistence.PagingQuery;

/**
 * 数据过滤器{@linkplain PagingQuery}。
 * 
 * @author datagear@163.com
 *
 */
public class DataFilterPagingQuery extends PagingQuery
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_DATA_FILTER = "dataFilter";

	private String dataFilter;

	public DataFilterPagingQuery()
	{
		super();
	}

	public DataFilterPagingQuery(int page)
	{
		super(page);
	}

	public DataFilterPagingQuery(int page, int pageSize)
	{
		super(page, pageSize);
	}

	public DataFilterPagingQuery(int page, int pageSize, String keyword)
	{
		super(page, pageSize, keyword);
	}

	public DataFilterPagingQuery(int page, int pageSize, String keyword, String condition)
	{
		super(page, pageSize, keyword, condition);
	}

	public String getDataFilter()
	{
		return dataFilter;
	}

	public void setDataFilter(String dataFilter)
	{
		this.dataFilter = dataFilter;
	}
}
