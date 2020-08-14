/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.connection.ConnectionSource;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.JsonFileDataSetEntity;
import org.datagear.management.domain.JsonValueDataSetEntity;
import org.datagear.management.domain.SchemaConnectionFactory;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.SummaryDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SchemaService;
import org.datagear.util.FileUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain DataSetEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetEntityServiceImpl extends AbstractMybatisDataPermissionEntityService<String, DataSetEntity>
		implements DataSetEntityService
{
	protected static final String SQL_NAMESPACE = DataSetEntity.class.getName();

	private ConnectionSource connectionSource;

	private SchemaService schemaService;

	private AuthorizationService authorizationService;

	/** 数据集文件存储根目录 */
	private File dataSetRootDirectory;

	public DataSetEntityServiceImpl()
	{
		super();
	}

	public DataSetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, ConnectionSource connectionSource,
			SchemaService schemaService, AuthorizationService authorizationService, File dataSetRootDirectory)
	{
		super(sqlSessionFactory);
		this.connectionSource = connectionSource;
		this.schemaService = schemaService;
		this.authorizationService = authorizationService;
		setDataSetRootDirectory(dataSetRootDirectory);
	}

	public DataSetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, ConnectionSource connectionSource,
			SchemaService schemaService, AuthorizationService authorizationService, File dataSetRootDirectory)
	{
		super(sqlSessionTemplate);
		this.connectionSource = connectionSource;
		this.schemaService = schemaService;
		this.authorizationService = authorizationService;
		setDataSetRootDirectory(dataSetRootDirectory);
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

	public File getDataSetRootDirectory()
	{
		return dataSetRootDirectory;
	}

	public void setDataSetRootDirectory(File dataSetRootDirectory)
	{
		this.dataSetRootDirectory = dataSetRootDirectory;
	}

	@Override
	public File getDataSetDirectory(String dataSetId)
	{
		return FileUtil.getDirectory(getDataSetRootDirectory(), dataSetId);
	}

	@Override
	public DataSet getDataSet(String id)
	{
		DataSetEntity entity = getById(id);

		if (entity instanceof SqlDataSetEntity)
		{
			SqlDataSetEntity sqlDataSetEntity = (SqlDataSetEntity) entity;

			SchemaConnectionFactory connectionFactory = sqlDataSetEntity.getConnectionFactory();

			connectionFactory.setSchema(this.schemaService.getById(connectionFactory.getSchema().getId()));
			connectionFactory.setConnectionSource(this.connectionSource);
		}

		return entity;
	}

	@Override
	protected boolean add(DataSetEntity entity, Map<String, Object> params)
	{
		if (entity instanceof SummaryDataSetEntity)
			throw new IllegalArgumentException();

		boolean success = super.add(entity, params);

		if (success)
		{
			if (entity instanceof SqlDataSetEntity)
				success = addSqlDataSetEntity((SqlDataSetEntity) entity);
			else if (entity instanceof JsonValueDataSetEntity)
				success = addJsonValueDataSetEntity((JsonValueDataSetEntity) entity);
			else if (entity instanceof JsonFileDataSetEntity)
				success = addJsonFileDataSetEntity((JsonFileDataSetEntity) entity);
		}

		if (success)
			saveDataSetChildren(entity);

		return success;
	}

	protected boolean addSqlDataSetEntity(SqlDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("insertSqlDataSetEntity", params) > 0);
	}

	protected boolean addJsonValueDataSetEntity(JsonValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("insertJsonValueDataSetEntity", params) > 0);
	}

	protected boolean addJsonFileDataSetEntity(JsonFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("insertJsonFileDataSetEntity", params) > 0);
	}

	@Override
	protected boolean update(DataSetEntity entity, Map<String, Object> params)
	{
		if (entity instanceof SummaryDataSetEntity)
			throw new IllegalArgumentException();

		boolean success = super.update(entity, params);

		if (success)
		{
			if (entity instanceof SqlDataSetEntity)
				success = updateSqlDataSetEntity((SqlDataSetEntity) entity);
			else if (entity instanceof JsonValueDataSetEntity)
				success = updateJsonValueDataSetEntity((JsonValueDataSetEntity) entity);
			else if (entity instanceof JsonFileDataSetEntity)
				success = updateJsonFileDataSetEntity((JsonFileDataSetEntity) entity);
		}

		if (success)
			saveDataSetChildren(entity);

		return success;
	}

	protected boolean updateSqlDataSetEntity(SqlDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("updateSqlDataSetEntity", params) > 0);
	}

	protected boolean updateJsonValueDataSetEntity(JsonValueDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("updateJsonValueDataSetEntity", params) > 0);
	}

	protected boolean updateJsonFileDataSetEntity(JsonFileDataSetEntity entity)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("entity", entity);

		return (updateMybatis("updateJsonFileDataSetEntity", params) > 0);
	}

	@Override
	public String getResourceType()
	{
		return SqlDataSetEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public DataSetEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return getById(user, id);
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
	protected void postProcessSelects(List<DataSetEntity> list)
	{
		// XXX 查询操作仅用于展示，不必完全加载
		// super.postProcessSelects(list);
	}

	@Override
	protected DataSetEntity postProcessSelect(DataSetEntity obj)
	{
		if (obj == null)
			return null;

		if (DataSetEntity.DATA_SET_TYPE_SQL.equals(obj.getDataSetType()))
			obj = getSqlDataSetEntityById(obj.getId());
		else if (DataSetEntity.DATA_SET_TYPE_JsonValue.equals(obj.getDataSetType()))
			obj = getJsonValueDataSetEntityById(obj.getId());
		else if (DataSetEntity.DATA_SET_TYPE_JsonFile.equals(obj.getDataSetType()))
			obj = getJsonFileDataSetEntityById(obj.getId());

		if (obj == null)
			return null;

		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("dataSetId", obj.getId());

		List<DataSetPropertyPO> propertyPOs = selectListMybatis("getPropertyPOs", params);
		List<DataSetProperty> dataSetProperties = DataSetPropertyPO.to(propertyPOs);
		obj.setProperties(dataSetProperties);

		List<DataSetParamPO> paramPOs = selectListMybatis("getParamPOs", params);
		List<DataSetParam> dataSetParams = DataSetParamPO.to(paramPOs);
		obj.setParams(dataSetParams);

		return obj;
	}

	protected SqlDataSetEntity getSqlDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("id", id);

		SqlDataSetEntity entity = selectOneMybatis("getSqlDataSetEntityById", params);

		return entity;
	}

	protected JsonValueDataSetEntity getJsonValueDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("id", id);

		JsonValueDataSetEntity entity = selectOneMybatis("getJsonValueDataSetEntityById", params);

		return entity;
	}

	protected JsonFileDataSetEntity getJsonFileDataSetEntityById(String id)
	{
		Map<String, Object> params = buildParamMapWithIdentifierQuoteParameter();
		params.put("id", id);

		JsonFileDataSetEntity entity = selectOneMybatis("getJsonFileDataSetEntityById", params);

		if (entity != null)
			entity.setDirectory(getDataSetDirectory(id));

		return entity;
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

	protected void saveDataSetChildren(DataSetEntity entity)
	{
		saveDataSetPropertyPOs(entity);
		saveDataSetParamPOs(entity);
	}

	protected void saveDataSetPropertyPOs(DataSetEntity entity)
	{
		deleteMybatis("deletePropertyPOs", entity.getId());

		List<DataSetPropertyPO> pos = DataSetPropertyPO.from(entity);

		if (!pos.isEmpty())
		{
			for (DataSetPropertyPO relation : pos)
				insertMybatis("insertPropertyPO", relation);
		}
	}

	protected void saveDataSetParamPOs(DataSetEntity entity)
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
			List<T> childs = new ArrayList<>();

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
			List<DataSetPropertyPO> pos = new ArrayList<>();

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
			List<DataSetParamPO> pos = new ArrayList<>();

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
