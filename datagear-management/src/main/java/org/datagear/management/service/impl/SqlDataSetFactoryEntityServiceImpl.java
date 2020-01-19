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
import org.datagear.analysis.DataSetExport;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
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
	protected boolean add(SqlDataSetFactoryEntity entity, Map<String, Object> params)
	{
		boolean success = super.add(entity, params);

		if (success)
			saveDataSetFactoryChildren(entity);

		return success;
	}

	@Override
	protected boolean update(SqlDataSetFactoryEntity entity, Map<String, Object> params)
	{
		boolean success = super.update(entity, params);

		if (success)
			saveDataSetFactoryChildren(entity);

		return success;
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

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
		{
			this.authorizationService.deleteByResource(SqlDataSetFactoryEntity.AUTHORIZATION_RESOURCE_TYPE, id);
		}

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

		Map<String, Object> sqlParams = buildParamMapWithIdentifierQuoteParameter();
		sqlParams.put("dataSetFactoryId", obj.getId());

		List<DataSetPropertyPO> propertyPOs = selectListMybatis("getPropertyPOs", sqlParams);
		List<DataSetProperty> dataSetProperties = DataSetPropertyPO.to(propertyPOs);
		obj.setProperties(dataSetProperties);

		List<DataSetParamPO> paramPOs = selectListMybatis("getParamPOs", sqlParams);
		List<DataSetParam> dataSetParams = DataSetParamPO.to(paramPOs);
		obj.setParams(dataSetParams);

		List<DataSetExportPO> exportPOs = selectListMybatis("getExportPOs", sqlParams);
		List<DataSetExport> dataSetExports = DataSetExportPO.to(exportPOs);
		obj.setExports(dataSetExports);
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

	protected void saveDataSetFactoryChildren(SqlDataSetFactory entity)
	{
		saveDataSetPropertyPOs(entity);
		saveDataSetParamPOs(entity);
		saveDataSetExportPOs(entity);
	}

	protected void saveDataSetPropertyPOs(SqlDataSetFactory entity)
	{
		deleteMybatis("deletePropertyPOs", entity.getId());

		List<DataSetPropertyPO> pos = DataSetPropertyPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetPropertyPO relation : pos)
				insertMybatis("insertPropertyPO", relation);
		}
	}

	protected void saveDataSetParamPOs(SqlDataSetFactory entity)
	{
		deleteMybatis("deleteParamPOs", entity.getId());

		List<DataSetParamPO> pos = DataSetParamPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetParamPO relation : pos)
				insertMybatis("insertParamPO", relation);
		}
	}

	protected void saveDataSetExportPOs(SqlDataSetFactory entity)
	{
		deleteMybatis("deleteExportPOs", entity.getId());

		List<DataSetExportPO> pos = DataSetExportPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetExportPO relation : pos)
				insertMybatis("insertExportPO", relation);
		}
	}

	public static abstract class DataSetFactoryChildPO<T>
	{
		private String dataSetFactoryId;
		private T child;
		private int order = 0;

		public DataSetFactoryChildPO()
		{
			super();
		}

		public DataSetFactoryChildPO(String dataSetFactoryId, T child, int order)
		{
			super();
			this.dataSetFactoryId = dataSetFactoryId;
			this.child = child;
			this.order = order;
		}

		public String getDataSetFactoryId()
		{
			return dataSetFactoryId;
		}

		public void setDataSetFactoryId(String dataSetFactoryId)
		{
			this.dataSetFactoryId = dataSetFactoryId;
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

		public static <T> List<T> to(List<? extends DataSetFactoryChildPO<T>> pos)
		{
			List<T> childs = new ArrayList<T>();

			if (pos != null)
			{
				for (DataSetFactoryChildPO<T> po : pos)
					childs.add(po.getChild());
			}

			return childs;
		}
	}

	public static class DataSetPropertyPO extends DataSetFactoryChildPO<DataSetProperty>
	{
		public DataSetPropertyPO()
		{
			super();
		}

		public DataSetPropertyPO(String dataSetFactoryId, DataSetProperty child, int order)
		{
			super(dataSetFactoryId, child, order);
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

		public static List<DataSetPropertyPO> from(DataSetFactory dataSetFactory)
		{
			List<DataSetPropertyPO> pos = new ArrayList<DataSetPropertyPO>();

			List<DataSetProperty> properties = dataSetFactory.getProperties();

			if (properties != null)
			{
				for (int i = 0; i < properties.size(); i++)
				{
					DataSetPropertyPO po = new DataSetPropertyPO(dataSetFactory.getId(), properties.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}

	public static class DataSetParamPO extends DataSetFactoryChildPO<DataSetParam>
	{
		public DataSetParamPO()
		{
			super();
		}

		public DataSetParamPO(String dataSetFactoryId, DataSetParam child, int order)
		{
			super(dataSetFactoryId, child, order);
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

		public static List<DataSetParamPO> from(DataSetFactory dataSetFactory)
		{
			List<DataSetParamPO> pos = new ArrayList<DataSetParamPO>();

			List<DataSetParam> params = dataSetFactory.getParams();

			if (params != null)
			{
				for (int i = 0; i < params.size(); i++)
				{
					DataSetParamPO po = new DataSetParamPO(dataSetFactory.getId(), params.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}

	public static class DataSetExportPO extends DataSetFactoryChildPO<DataSetExport>
	{
		public DataSetExportPO()
		{
			super();
		}

		public DataSetExportPO(String dataSetFactoryId, DataSetExport child, int order)
		{
			super(dataSetFactoryId, child, order);
		}

		@Override
		public DataSetExport getChild()
		{
			return super.getChild();
		}

		@Override
		public void setChild(DataSetExport child)
		{
			super.setChild(child);
		}

		public static List<DataSetExportPO> from(DataSetFactory dataSetFactory)
		{
			List<DataSetExportPO> pos = new ArrayList<DataSetExportPO>();

			List<DataSetExport> exports = dataSetFactory.getExports();

			if (exports != null)
			{
				for (int i = 0; i < exports.size(); i++)
				{
					DataSetExportPO po = new DataSetExportPO(dataSetFactory.getId(), exports.get(i), i);
					pos.add(po);
				}
			}

			return pos;
		}
	}
}
