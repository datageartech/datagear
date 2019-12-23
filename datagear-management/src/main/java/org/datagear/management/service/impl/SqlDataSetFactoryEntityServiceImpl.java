/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.support.SqlDataSetFactory;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.SchemaConnectionFactory;
import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlDataSetFactoryEntityService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SqlDataSetFactoryEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFactoryEntityServiceImpl
		extends AbstractMybatisDataPermissionEntityService<String, SqlDataSetFactoryEntity>
		implements SqlDataSetFactoryEntityService
{
	protected static final String SQL_NAMESPACE = SqlDataSetFactoryEntity.class.getName();

	private ConnectionSource connectionSource;

	private SchemaService schemaService;

	private AuthorizationService authorizationService;

	public SqlDataSetFactoryEntityServiceImpl()
	{
		super();
	}

	public SqlDataSetFactoryEntityServiceImpl(SqlSessionFactory sqlSessionFactory, ConnectionSource connectionSource,
			SchemaService schemaService, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.connectionSource = connectionSource;
		this.schemaService = schemaService;
		this.authorizationService = authorizationService;
	}

	public SqlDataSetFactoryEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, ConnectionSource connectionSource,
			SchemaService schemaService, AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
		this.connectionSource = connectionSource;
		this.schemaService = schemaService;
		this.authorizationService = authorizationService;
	}

	public ConnectionSource getConnectionSource()
	{
		return connectionSource;
	}

	public void setConnectionSource(ConnectionSource connectionSource)
	{
		this.connectionSource = connectionSource;
	}

	public SchemaService getSchemaService()
	{
		return schemaService;
	}

	public void setSchemaService(SchemaService schemaService)
	{
		this.schemaService = schemaService;
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
	public SqlDataSetFactory getSqlDataSetFactory(String id)
	{
		return getById(id);
	}

	@Override
	public SqlDataSetFactoryEntity[] getSqlDataSetFactoryEntities(String htmlChartWidgetEntityId)
	{
		List<SqlDataSetFactoryEntity> list = getSqlDataSetFactoryEntityList(htmlChartWidgetEntityId);

		SqlDataSetFactoryEntity[] array = new SqlDataSetFactoryEntity[list == null ? 0 : list.size()];

		list.toArray(array);

		return array;
	}

	@Override
	public SqlDataSetFactory[] getSqlDataSetFactories(String chartWidgetId)
	{
		List<SqlDataSetFactoryEntity> list = getSqlDataSetFactoryEntityList(chartWidgetId);
		for (SqlDataSetFactoryEntity entry : list)
			postProcessSelect(entry);

		SqlDataSetFactoryEntity[] array = new SqlDataSetFactoryEntity[list.size()];
		list.toArray(array);

		return array;
	}

	@Override
	public String getResourceType()
	{
		return SqlDataSetFactoryEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public SqlDataSetFactoryEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	public List<SqlDataSetFactoryEntity> getSqlDataSetFactoryEntityList(String htmlChartWidgetEntityId)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("htmlChartWidgetEntityId", htmlChartWidgetEntityId);

		List<SqlDataSetFactoryEntity> list = super.query("findByHtmlChartWidgetEntityId", params);

		return list;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.authorizationService.deleteByResource(SqlDataSetFactoryEntity.AUTHORIZATION_RESOURCE_TYPE, id);

		return deleted;
	}

	@Override
	protected void postProcessSelects(List<SqlDataSetFactoryEntity> list)
	{
		// XXX 查询操作仅用于展示，不必完全加载
		// super.postProcessSelects(list);
	}

	@Override
	protected void postProcessSelect(SqlDataSetFactoryEntity obj)
	{
		if (obj == null)
			return;

		SchemaConnectionFactory connectionFactory = obj.getConnectionFactory();

		connectionFactory.setSchema(this.schemaService.getById(connectionFactory.getSchema().getId()));
		connectionFactory.setConnectionSource(this.connectionSource);
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
