/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
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

	public SchemaServiceImpl(SqlSessionFactory sqlSessionFactory, DriverEntityManager driverEntityManager,
			AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.driverEntityManager = driverEntityManager;
		this.authorizationService = authorizationService;
	}

	public SchemaServiceImpl(SqlSessionTemplate sqlSessionTemplate, DriverEntityManager driverEntityManager,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
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
	protected Schema getById(String id, Map<String, Object> params)
	{
		Schema schema = super.getById(id, params);

		setDriverEntityActual(schema);

		return schema;
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
	protected List<Schema> query(String statement, Query query, Map<String, Object> params)
	{
		List<Schema> schemas = super.query(statement, query, params);

		for (Schema schema : schemas)
			setDriverEntityActual(schema);

		return schemas;
	}

	@Override
	protected PagingData<Schema> pagingQuery(String statement, PagingQuery pagingQuery, Map<String, Object> params)
	{
		PagingData<Schema> pagingData = super.pagingQuery(statement, pagingQuery, params);

		List<Schema> items = pagingData.getItems();

		for (Schema schema : items)
			setDriverEntityActual(schema);

		return pagingData;
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
	public int deleteByUserId(String... userIds)
	{
		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("userIds", userIds);

		return updateMybatis("deleteByUserId", params);
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
		Integer permission = this.authorizationService.getPermissionForPatternSource(user, getResourceType(), url);

		if (permission == null || Schema.canDeleteTableData(permission))
			return;

		throw new SaveSchemaUrlPermissionDeniedException();
	}

	/**
	 * 设置{@linkplain DriverEntity}。
	 * 
	 * @param schema
	 */
	protected void setDriverEntityActual(Schema schema)
	{
		if (schema == null)
			return;

		if (schema.hasDriverEntity())
		{
			DriverEntity driverEntity = this.driverEntityManager.get(schema.getDriverEntity().getId());
			schema.setDriverEntity(driverEntity);
		}
	}

	@Override
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		addDataPermissionParameters(params, user, getResourceType(), true, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
