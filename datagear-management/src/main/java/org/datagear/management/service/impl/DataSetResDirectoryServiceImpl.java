/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain DataSetResDirectoryService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResDirectoryServiceImpl extends
		AbstractMybatisDataPermissionEntityService<String, DataSetResDirectory> implements DataSetResDirectoryService
{
	protected static final String SQL_NAMESPACE = DataSetResDirectory.class.getName();

	public DataSetResDirectoryServiceImpl()
	{
		super();
	}

	public DataSetResDirectoryServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
	}

	public DataSetResDirectoryServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
	}

	@Override
	public String getResourceType()
	{
		return DataSetResDirectory.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public DataSetResDirectory getByStringId(User user, String id) throws PermissionDeniedException
	{
		return getById(user, id);
	}

	@Override
	public int updateCreateUserId(String oldUserId, String newUserId)
	{
		Map<String, Object> params = buildParamMap();
		params.put("oldUserId", oldUserId);
		params.put("newUserId", newUserId);

		return updateMybatis("updateCreateUserId", params);
	}

	@Override
	protected void checkAddInput(DataSetResDirectory entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getDirectory()) || isEmpty(entity.getCreateUser()))
			throw new IllegalArgumentException();
	}

	@Override
	protected void checkUpdateInput(DataSetResDirectory entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getDirectory()))
			throw new IllegalArgumentException();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
