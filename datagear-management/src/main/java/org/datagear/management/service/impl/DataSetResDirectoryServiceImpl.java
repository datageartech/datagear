/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.management.service.impl;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.DataSetResDirectory;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetResDirectoryService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.UserService;
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

	private UserService userService;

	public DataSetResDirectoryServiceImpl()
	{
		super();
	}

	public DataSetResDirectoryServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService, UserService userService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.userService = userService;
	}

	public DataSetResDirectoryServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService, UserService userService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
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
		return super.updateCreateUserId(oldUserId, newUserId);
	}

	@Override
	public int updateCreateUserId(String[] oldUserIds, String newUserId)
	{
		return super.updateCreateUserId(oldUserIds, newUserId);
	}

	@Override
	protected DataSetResDirectory postProcessGet(DataSetResDirectory obj)
	{
		inflateCreateUserEntity(obj, this.userService);
		return super.postProcessGet(obj);
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
