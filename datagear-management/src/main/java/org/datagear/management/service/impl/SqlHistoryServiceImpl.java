/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.SqlHistory;
import org.datagear.management.service.SqlHistoryService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SqlHistoryService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlHistoryServiceImpl extends AbstractMybatisEntityService<String, SqlHistory> implements SqlHistoryService
{
	/** 默认最多保留SQL历史个数 */
	public static final int HISTORY_REMAIN = 200;

	protected static final String SQL_NAMESPACE = SqlHistory.class.getName();

	public SqlHistoryServiceImpl()
	{
		super();
	}

	public SqlHistoryServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super(sqlSessionFactory, dialect);
	}

	public SqlHistoryServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super(sqlSessionTemplate, dialect);
	}

	@Override
	public void addForRemain(String schemaId, String userId, List<String> sqls)
	{
		for (int i = 0, len = sqls.size(); i < len; i++)
		{
			SqlHistory sqlHistory = new SqlHistory(IDUtil.randomIdOnTime20(), sqls.get(i), schemaId, userId);
			add(sqlHistory);
		}

		deleteExpired(schemaId, userId, HISTORY_REMAIN);
	}

	@Override
	public PagingData<SqlHistory> pagingQueryByUserId(String schemaId, String userId, PagingQuery pagingQuery)
	{
		Map<String, Object> params = buildParamMap();
		params.put("schemaId", schemaId);
		params.put("userId", userId);

		if (isEmpty(pagingQuery.getOrders()))
			addOrderCreateTimeDesc(params);

		return pagingQuery(pagingQuery, params);
	}

	protected int deleteExpired(String schemaId, String userId, int maximum)
	{
		Map<String, Object> param = buildParamMap();
		param.put("schemaId", schemaId);
		param.put("userId", userId);

		addPagingQueryParams(param, 0, HISTORY_REMAIN);

		// 如果不支持分页，则删除30天以前的历史
		if (!getDialect().supportsPaging())
		{
			long time = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30;
			Date deleteBeforeDate = new Date(time);
			param.put("deleteBeforeDate", deleteBeforeDate);
		}

		return deleteMybatis("deleteExpired", param);
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
