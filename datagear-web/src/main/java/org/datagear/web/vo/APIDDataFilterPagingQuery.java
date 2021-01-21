/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
