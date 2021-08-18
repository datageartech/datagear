/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain AnalysisProjectService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class AnalysisProjectServiceImpl extends AbstractMybatisDataPermissionEntityService<String, AnalysisProject>
		implements AnalysisProjectService
{
	protected static final String SQL_NAMESPACE = AnalysisProject.class.getName();

	public AnalysisProjectServiceImpl()
	{
		super();
	}

	public AnalysisProjectServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
	}

	public AnalysisProjectServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
	}

	@Override
	public String getResourceType()
	{
		return AnalysisProject.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public AnalysisProject getByStringId(User user, String id) throws PermissionDeniedException
	{
		return getById(user, id);
	}

	@Override
	public int updateCreateUserId(String oldUserId, String newUserId)
	{
		return super.updateCreateUserId(oldUserId, newUserId);
	}

	@Override
	protected void checkAddInput(AnalysisProject entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getName()) || isEmpty(entity.getCreateUser()))
			throw new IllegalArgumentException();
	}

	@Override
	protected void checkUpdateInput(AnalysisProject entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getName()))
			throw new IllegalArgumentException();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
