/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaGuardService;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.UserService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SchemaService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaServiceImpl extends AbstractMybatisDataPermissionEntityService<String, Schema>
		implements SchemaService
{
	protected static final String SQL_NAMESPACE = Schema.class.getName();

	private DriverEntityManager driverEntityManager;

	private UserService userService;

	private SchemaGuardService schemaGuardService;

	public SchemaServiceImpl()
	{
		super();
	}

	public SchemaServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			DriverEntityManager driverEntityManager, UserService userService, SchemaGuardService schemaGuardService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.driverEntityManager = driverEntityManager;
		this.userService = userService;
		this.schemaGuardService = schemaGuardService;
	}

	public SchemaServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			DriverEntityManager driverEntityManager, UserService userService, SchemaGuardService schemaGuardService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.driverEntityManager = driverEntityManager;
		this.userService = userService;
		this.schemaGuardService = schemaGuardService;
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

	public SchemaGuardService getSchemaGuardService()
	{
		return schemaGuardService;
	}

	public void setSchemaGuardService(SchemaGuardService schemaGuardService)
	{
		this.schemaGuardService = schemaGuardService;
	}

	@Override
	public String getResourceType()
	{
		return Schema.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public void add(User user, Schema entity) throws PermissionDeniedException
	{
		checkSaveUrlPermission(user, entity.getUrl());
		super.add(user, entity);
	}

	@Override
	public boolean update(User user, Schema entity) throws PermissionDeniedException
	{
		checkSaveUrlPermission(user, entity.getUrl());
		return super.update(user, entity);
	}

	@Override
	public Schema getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	@Override
	public int updateCreateUserId(String oldUserId, String newUserId)
	{
		return super.updateCreateUserId(oldUserId, newUserId);
	}

	@Override
	public int deleteByUserId(String... userIds)
	{
		Map<String, Object> params = buildParamMap();
		params.put("userIds", userIds);

		int count = updateMybatis("deleteByUserId", params);

		if (count > 0)
			cacheInvalidate();

		return count;
	}

	@Override
	protected Schema postProcessGet(Schema schema)
	{
		inflateCreateUserEntity(schema, this.userService);

		if (schema.hasDriverEntity())
		{
			String did = schema.getDriverEntity().getId();

			if (!StringUtil.isEmpty(did))
				schema.setDriverEntity(this.driverEntityManager.get(did));
		}

		return super.postProcessGet(schema);
	}

	@Override
	protected void checkInput(Schema entity)
	{
		if (isBlank(entity.getId()) || isBlank(entity.getTitle()) || isBlank(entity.getUrl()))
			throw new IllegalArgumentException();
	}

	/**
	 * 校验用户是否有权保存指定URL的{@linkplain Schema}。
	 * 
	 * @param user
	 * @param url
	 * @throws SaveSchemaUrlPermissionDeniedException
	 */
	protected void checkSaveUrlPermission(User user, String url) throws SaveSchemaUrlPermissionDeniedException
	{
		if (user.isAdmin())
			return;

		if (this.schemaGuardService.isPermitted(url))
			return;

		throw new SaveSchemaUrlPermissionDeniedException();
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
