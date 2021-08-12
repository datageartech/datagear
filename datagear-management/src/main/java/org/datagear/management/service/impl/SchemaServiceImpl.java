/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.management.util.dialect.MbSqlDialect;
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

	private AuthorizationService authorizationService;

	public SchemaServiceImpl()
	{
		super();
	}

	public SchemaServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			DriverEntityManager driverEntityManager, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory, dialect);
		this.driverEntityManager = driverEntityManager;
		this.authorizationService = authorizationService;
	}

	public SchemaServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			DriverEntityManager driverEntityManager, AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate, dialect);
		this.driverEntityManager = driverEntityManager;
		this.authorizationService = authorizationService;
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	@Override
	public String getResourceType()
	{
		return Schema.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public boolean add(User user, Schema entity) throws PermissionDeniedException
	{
		checkSaveUrlPermission(user, entity.getUrl());

		return super.add(user, entity);
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
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.authorizationService.deleteByResource(Schema.AUTHORIZATION_RESOURCE_TYPE, id);

		return deleted;
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
	public int deleteByUserId(String... userIds)
	{
		Map<String, Object> params = buildParamMap();
		params.put("userIds", userIds);

		return updateMybatis("deleteByUserId", params);
	}

	@Override
	protected Schema postProcessSelect(Schema schema)
	{
		if (schema != null && schema.hasDriverEntity())
		{
			DriverEntity driverEntity = this.driverEntityManager.get(schema.getDriverEntity().getId());
			schema.setDriverEntity(driverEntity);
		}

		return schema;
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
		// TODO 新增新建数据源URL控制功能，管理员可设置，这里判断
		throw new SaveSchemaUrlPermissionDeniedException();
	}

	@Override
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		addDataPermissionParameters(params, user, getResourceType(), true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
