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

package org.datagear.web.vo;

import org.datagear.management.domain.AnalysisProject;

/**
 * 包含{@linkplain AnalysisProject#getId()}信息的查询。
 * 
 * @author datagear@163.com
 *
 */
public class APIDDataFilterPagingQuery extends DataFilterPagingQuery
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_APID = "analysisProjectId";

	/** 查询的AnalysisProject ID */
	private String analysisProjectId = null;

	public APIDDataFilterPagingQuery()
	{
		super();
	}

	public APIDDataFilterPagingQuery(int page)
	{
		super(page);
	}

	public APIDDataFilterPagingQuery(int page, int pageSize)
	{
		super(page, pageSize);
	}

	public APIDDataFilterPagingQuery(int page, int pageSize, String keyword)
	{
		super(page, pageSize, keyword);
	}

	public APIDDataFilterPagingQuery(int page, int pageSize, String keyword, String condition)
	{
		super(page, pageSize, keyword, condition);
	}

	public String getAnalysisProjectId()
	{
		return analysisProjectId;
	}

	public void setAnalysisProjectId(String analysisProjectId)
	{
		this.analysisProjectId = analysisProjectId;
	}
}
