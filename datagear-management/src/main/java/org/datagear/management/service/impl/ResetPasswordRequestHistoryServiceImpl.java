/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.Date;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.ResetPasswordRequestHistory;
import org.datagear.management.service.ResetPasswordRequestHistoryService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain ResetPasswordRequestHistoryService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class ResetPasswordRequestHistoryServiceImpl extends
		AbstractMybatisEntityService<String, ResetPasswordRequestHistory> implements ResetPasswordRequestHistoryService
{
	protected static final String SQL_NAMESPACE = ResetPasswordRequestHistory.class.getName();

	public ResetPasswordRequestHistoryServiceImpl()
	{
		super();
	}

	public ResetPasswordRequestHistoryServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public ResetPasswordRequestHistoryServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	public boolean add(ResetPasswordRequestHistory entity)
	{
		return super.add(entity);
	}

	@Override
	public ResetPasswordRequestHistory get(String resetPasswordRequestId)
	{
		return super.getById(resetPasswordRequestId);
	}

	@Override
	public int deleteBefore(Date createDate)
	{
		return super.deleteMybatis("deleteBefore", createDate);
	}

	@Override
	public PagingData<ResetPasswordRequestHistory> pagingQuery(PagingQuery pagingQuery)
	{
		return super.pagingQuery(pagingQuery);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
