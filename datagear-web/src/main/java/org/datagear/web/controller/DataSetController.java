/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.service.SqlDataSetFactoryEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据集控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysis/dataSet")
public class DataSetController extends AbstractController
{
	@Autowired
	private SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService;

	public DataSetController()
	{
		super();
	}

	public DataSetController(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		super();
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
	}

	public SqlDataSetFactoryEntityService getSqlDataSetFactoryEntityService()
	{
		return sqlDataSetFactoryEntityService;
	}

	public void setSqlDataSetFactoryEntityService(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, org.springframework.ui.Model model)
	{
		return "/analysis/dataSet/dataSet_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<SqlDataSetFactoryEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel) throws Exception
	{
		PagingQuery pagingQuery = getPagingQuery(request, WebUtils.COOKIE_PAGINATION_SIZE);

		PagingData<SqlDataSetFactoryEntity> pagingData = this.sqlDataSetFactoryEntityService.pagingQuery(pagingQuery);

		return pagingData;
	}
}
