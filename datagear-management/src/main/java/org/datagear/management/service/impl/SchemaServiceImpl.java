/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
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

	public SchemaServiceImpl()
	{
		super();
	}

	public SchemaServiceImpl(SqlSessionFactory sqlSessionFactory, DriverEntityManager driverEntityManager)
	{
		super(sqlSessionFactory);
		this.driverEntityManager = driverEntityManager;
	}

	public SchemaServiceImpl(SqlSessionTemplate sqlSessionTemplate, DriverEntityManager driverEntityManager)
	{
		super(sqlSessionTemplate);
		this.driverEntityManager = driverEntityManager;
	}

	public DriverEntityManager getDriverEntityManager()
	{
		return driverEntityManager;
	}

	public void setDriverEntityManager(DriverEntityManager driverEntityManager)
	{
		this.driverEntityManager = driverEntityManager;
	}

	@Override
	protected Schema getById(String id, Map<String, Object> params)
	{
		Schema schema = super.getById(id, params);

		setDriverEntityActual(schema);

		return schema;
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
		addDataPermissionParameters(params, user, Authorization.RESOURCE_TYPE_DATA_SOURCE, true, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
