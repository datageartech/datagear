/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
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
