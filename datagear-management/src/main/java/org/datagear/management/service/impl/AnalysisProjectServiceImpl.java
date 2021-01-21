/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.PermissionDeniedException;
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

	public AnalysisProjectServiceImpl(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public AnalysisProjectServiceImpl(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
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
		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("oldUserId", oldUserId);
		params.put("newUserId", newUserId);

		return updateMybatis("updateCreateUserId", params);
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
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		addDataPermissionParameters(params, user, getResourceType(), false, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
