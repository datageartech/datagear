/*
 * Copyright 2018-present datagear.tech
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

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.DtbsSource;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DtbsSourceGuardService;
import org.datagear.management.service.DtbsSourceService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.UserService;
import org.datagear.management.util.GuardEntity;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * {@linkplain DtbsSourceService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceServiceImpl extends AbstractMybatisDataPermissionEntityService<String, DtbsSource>
		implements DtbsSourceService
{
	protected static final String SQL_NAMESPACE = DtbsSource.class.getName();

	private DriverEntityManager driverEntityManager;

	private UserService userService;

	private DtbsSourceGuardService dtbsSourceGuardService;

	private TextEncryptor textEncryptor = null;

	public DtbsSourceServiceImpl()
	{
		super();
	}

	public DtbsSourceServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService, DriverEntityManager driverEntityManager, UserService userService,
			DtbsSourceGuardService dtbsSourceGuardService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.driverEntityManager = driverEntityManager;
		this.userService = userService;
		this.dtbsSourceGuardService = dtbsSourceGuardService;
	}

	public DtbsSourceServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService, DriverEntityManager driverEntityManager, UserService userService,
			DtbsSourceGuardService dtbsSourceGuardService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.driverEntityManager = driverEntityManager;
		this.userService = userService;
		this.dtbsSourceGuardService = dtbsSourceGuardService;
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public DtbsSourceGuardService getStbsSourceGuardService()
	{
		return dtbsSourceGuardService;
	}

	public void setStbsSourceGuardService(DtbsSourceGuardService dtbsSourceGuardService)
	{
		this.dtbsSourceGuardService = dtbsSourceGuardService;
	}

	public TextEncryptor getTextEncryptor()
	{
		return textEncryptor;
	}

	public void setTextEncryptor(TextEncryptor textEncryptor)
	{
		this.textEncryptor = textEncryptor;
	}

	@Override
	public String getResourceType()
	{
		return DtbsSource.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public void add(User user, DtbsSource entity) throws PermissionDeniedException
	{
		checkSaveUrlPermission(user, entity);
		super.add(user, entity);
	}

	@Override
	public boolean update(User user, DtbsSource entity) throws PermissionDeniedException
	{
		checkSaveUrlPermission(user, entity);
		return super.update(user, entity);
	}

	@Override
	public DtbsSource getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
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
	protected void add(DtbsSource entity, Map<String, Object> params)
	{
		if (this.textEncryptor != null)
		{
			entity = entity.clone();
			entity.setPassword(this.textEncryptor.encrypt(entity.getPassword()));
		}

		super.add(entity, params);
	}

	@Override
	protected boolean update(DtbsSource entity, Map<String, Object> params)
	{
		if (this.textEncryptor != null)
		{
			entity = entity.clone();
			entity.setPassword(this.textEncryptor.encrypt(entity.getPassword()));
		}

		return super.update(entity, params);
	}

	@Override
	protected DtbsSource getByIdFromDB(String id, Map<String, Object> params)
	{
		DtbsSource entity = super.getByIdFromDB(id, params);

		if (this.textEncryptor != null && entity != null)
			entity.setPassword(this.textEncryptor.decrypt(entity.getPassword()));

		return entity;
	}

	@Override
	protected DtbsSource postProcessGet(DtbsSource entity)
	{
		inflateCreateUserEntity(entity, this.userService);

		if (entity.hasDriverEntity())
		{
			String did = entity.getDriverEntity().getId();

			if (!StringUtil.isEmpty(did))
				entity.setDriverEntity(this.driverEntityManager.get(did));
		}

		return super.postProcessGet(entity);
	}

	@Override
	protected void checkInput(DtbsSource entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getTitle()) || isBlank(entity.getUrl()))
			throw new IllegalArgumentException();
	}

	/**
	 * 校验用户是否有权保存指定URL的{@linkplain DtbsSource}。
	 * 
	 * @param user
	 * @param url
	 * @throws SaveDtbsSourcePermissionDeniedException
	 */
	protected void checkSaveUrlPermission(User user, DtbsSource dtbsSource) throws SaveDtbsSourcePermissionDeniedException
	{
		if (this.dtbsSourceGuardService.isPermitted(user, new GuardEntity(dtbsSource)))
			return;

		throw new SaveDtbsSourcePermissionDeniedException();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
