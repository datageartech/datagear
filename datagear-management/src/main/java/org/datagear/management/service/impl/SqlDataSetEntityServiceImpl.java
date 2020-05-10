/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.SchemaConnectionFactory;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.management.service.SqlDataSetEntityService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain SqlDataSetEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetEntityServiceImpl extends AbstractMybatisDataPermissionEntityService<String, SqlDataSetEntity>
		implements SqlDataSetEntityService
{
	protected static final String SQL_NAMESPACE = SqlDataSetEntity.class.getName();

	private ConnectionSource connectionSource;

	private SchemaService schemaService;

	private AuthorizationService authorizationService;

	public SqlDataSetEntityServiceImpl()
	{
		super();
	}

	public SqlDataSetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, ConnectionSource connectionSource,
			SchemaService schemaService, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.connectionSource = connectionSource;
		this.schemaService = schemaService;
		this.authorizationService = authorizationService;
	}

	public SqlDataSetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, ConnectionSource connectionSource,
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
	public SqlDataSet getSqlDataSet(String id)
	{
		SqlDataSetEntity entity = getById(id);

		SchemaConnectionFactory connectionFactory = entity.getConnectionFactory();

		connectionFactory.setSchema(this.schemaService.getById(connectionFactory.getSchema().getId()));
		connectionFactory.setConnectionSource(this.connectionSource);

		return entity;
	}

	@Override
	protected boolean add(SqlDataSetEntity entity, Map<String, Object> params)
	{
		boolean success = super.add(entity, params);

		if (success)
			saveDataSetChildren(entity);

		return success;
	}

	@Override
	protected boolean update(SqlDataSetEntity entity, Map<String, Object> params)
	{
		boolean success = super.update(entity, params);

		if (success)
			saveDataSetChildren(entity);

		return success;
	}

	@Override
	public String getResourceType()
	{
		return SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public SqlDataSetEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
		{
			this.authorizationService.deleteByResource(SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE, id);
		}

		return deleted;
	}

	@Override
	protected void postProcessSelects(List<SqlDataSetEntity> list)
	{
		// XXX 查询操作仅用于展示，不必完全加载
		// super.postProcessSelects(list);
	}

	@Override
	protected void postProcessSelect(SqlDataSetEntity obj)
	{
		if (obj == null)
			return;

		Map<String, Object> sqlParams = buildParamMapWithIdentifierQuoteParameter();
		sqlParams.put("dataSetId", obj.getId());

		List<DataSetPropertyPO> propertyPOs = selectListMybatis("getPropertyPOs", sqlParams);
		List<DataSetProperty> dataSetProperties = DataSetPropertyPO.to(propertyPOs);
		obj.setProperties(dataSetProperties);

		List<DataSetParamPO> paramPOs = selectListMybatis("getParamPOs", sqlParams);
		List<DataSetParam> dataSetParams = DataSetParamPO.to(paramPOs);
		obj.setParams(dataSetParams);
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

	protected void saveDataSetChildren(SqlDataSet entity)
	{
		saveDataSetPropertyPOs(entity);
		saveDataSetParamPOs(entity);
	}

	protected void saveDataSetPropertyPOs(SqlDataSet entity)
	{
		deleteMybatis("deletePropertyPOs", entity.getId());

		List<DataSetPropertyPO> pos = DataSetPropertyPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetPropertyPO relation : pos)
				insertMybatis("insertPropertyPO", relation);
		}
	}

	protected void saveDataSetParamPOs(SqlDataSet entity)
	{
		deleteMybatis("deleteParamPOs", entity.getId());

		List<DataSetParamPO> pos = DataSetParamPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetParamPO relation : pos)
				insertMybatis("insertParamPO", relation);
		}
	}

	public static abstract class DataSetChildPO<T>
	{
		private String dataSetId;
		private T child;
		private int order = 0;

		public DataSetChildPO()
		{
			super();
		}

		public DataSetChildPO(String dataSetId, T child, int order)
		{
			super();
			this.dataSetId = dataSetId;
			this.child = child;
			this.order = order;
		}

		public String getDataSetId()
		{
			return dataSetId;
		}

		public void setDataSetId(String dataSetId)
		{
			this.dataSetId = dataSetId;
		}

		public T getChild()
		{
			return child;
		}

		public void setChild(T child)
		{
			this.child = child;
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		public static <T> List<T> to(List<? extends DataSetChildPO<T>> pos)
		{
			List<T> childs = new ArrayList<T>();

			if (pos != null)
			{
				for (DataSetChildPO<T> po : pos)
					childs.add(po.getChild());
			}

			return childs;
		}
	}

	public static class DataSetPropertyPO extends DataSetChildPO<DataSetProperty>
	{
		public DataSetPropertyPO()
		{
			super();
		}

		public DataSetPropertyPO(String dataSetId, DataSetProperty child, int order)
		{
			super(dataSetId, child, order);
		}

		@Override
		public DataSetProperty getChild()
		{
			return super.getChild();
		}

		@Override
		public void setChild(DataSetProperty child)
		{
			super.setChild(child);
		}

		public static List<DataSetPropertyPO> from(DataSet dataSet)
		{
			List<DataSetPropertyPO> pos = new ArrayList<DataSetPropertyPO>();

			List<DataSetProperty> properties = dataSet.getProperties();

			if (properties != null)
			{
				for (int i = 0; i < properties.size(); i++)
				{
					DataSetPropertyPO po = new DataSetPropertyPO(dataSet.getId(), properties.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}

	public static class DataSetParamPO extends DataSetChildPO<DataSetParam>
	{
		public DataSetParamPO()
		{
			super();
		}

		public DataSetParamPO(String dataSetId, DataSetParam child, int order)
		{
			super(dataSetId, child, order);
		}

		@Override
		public DataSetParam getChild()
		{
			return super.getChild();
		}

		@Override
		public void setChild(DataSetParam child)
		{
			super.setChild(child);
		}

		public static List<DataSetParamPO> from(DataSet dataSet)
		{
			List<DataSetParamPO> pos = new ArrayList<DataSetParamPO>();

			List<DataSetParam> params = dataSet.getParams();

			if (params != null)
			{
				for (int i = 0; i < params.size(); i++)
				{
					DataSetParamPO po = new DataSetParamPO(dataSet.getId(), params.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}
}
