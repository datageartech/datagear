/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.SqlHistory;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SqlHistoryService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlHistoryServiceImpl extends AbstractMybatisEntityService<String, SqlHistory> implements SqlHistoryService
{
	protected static final String SQL_NAMESPACE = SqlHistory.class.getName();

	public SqlHistoryServiceImpl()
	{
		super();
	}

	public SqlHistoryServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public SqlHistoryServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	public PagingData<SqlHistory> pagingQueryByUserId(String userId, PagingQuery pagingQuery)
	{
		Map<String, Object> params = buildParamMap();
		params.put("userId", userId);

		if (isEmpty(pagingQuery.getOrders()))
			addOrderCreateTimeDesc(params);

		return pagingQuery(pagingQuery, params);
	}

	protected void addOrderCreateTimeDesc(Map<String, Object> params)
	{
		params.put(QUERY_PARAM_ORDER, toQuoteIdentifier("createTime") + " DESC");
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
