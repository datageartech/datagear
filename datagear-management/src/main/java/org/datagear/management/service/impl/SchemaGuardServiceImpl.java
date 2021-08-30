/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.SchemaGuard;
import org.datagear.management.service.SchemaGuardService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SchemaGuardService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaGuardServiceImpl extends AbstractMybatisEntityService<String, SchemaGuard>
		implements SchemaGuardService
{
	protected static final String SQL_NAMESPACE = SchemaGuard.class.getName();

	public SchemaGuardServiceImpl()
	{
		super();
	}

	public SchemaGuardServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super(sqlSessionFactory, dialect);
	}

	public SchemaGuardServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super(sqlSessionTemplate, dialect);
	}

	@Override
	protected List<SchemaGuard> query(String statement, Map<String, Object> params)
	{
		List<SchemaGuard> list = super.query(statement, params);

		SchemaGuard.sortByPriority(list);

		return list;
	}

	@Override
	protected List<SchemaGuard> query(String statement, Map<String, Object> params, RowBounds rowBounds)
	{
		List<SchemaGuard> list = super.query(statement, params, rowBounds);

		SchemaGuard.sortByPriority(list);

		return list;
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
