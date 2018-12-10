/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.connection.DriverEntity;
import org.datagear.connection.DriverEntityManager;
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
public class SchemaServiceImpl extends AbstractMybatisEntityService<String, Schema> implements SchemaService
{
	protected static final String SQL_NAMESPACE = Schema.class.getName();

	private DriverEntityManager driverEntityManager;

	private SchemaCache schemaCache;

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

	public SchemaCache getSchemaCache()
	{
		return schemaCache;
	}

	public void setSchemaCache(SchemaCache schemaCache)
	{
		this.schemaCache = schemaCache;
	}

	@Override
	public boolean update(Schema entity)
	{
		return super.update(entity);
	}

	@Override
	public boolean update(User user, Schema entity)
	{
		if (this.schemaCache != null)
			this.schemaCache.removeSchema(entity.getId());

		return super.update(user, entity);
	}

	@Override
	public boolean deleteById(String id)
	{
		if (this.schemaCache != null)
			this.schemaCache.removeSchema(id);

		return super.deleteById(id);
	}

	@Override
	public boolean deleteById(User user, String id)
	{
		if (this.schemaCache != null)
			this.schemaCache.removeSchema(id);

		return super.deleteById(user, id);
	}

	@Override
	public Schema getById(String id)
	{
		Schema schema = (this.schemaCache == null ? null : this.schemaCache.getSchema(id));

		if (schema == null)
		{
			schema = super.getById(id);

			if (this.schemaCache != null)
				this.schemaCache.putSchema(schema);
		}

		return cloneIf(schema);
	}

	@Override
	public Schema getById(User user, String id)
	{
		Schema schema = (this.schemaCache == null ? null : this.schemaCache.getSchema(id));

		if (schema != null)
		{
			if (!schema.isShared() && !user.getId().equals(schema.getCreateUser().getId()))
				schema = null;
		}
		else
		{
			schema = super.getById(user, id);

			if (this.schemaCache != null)
				this.schemaCache.putSchema(schema);
		}

		return cloneIf(schema);
	}

	@Override
	public List<Schema> query(Query query)
	{
		return cloneIf(super.query(query));
	}

	@Override
	public List<Schema> query(User user, Query query)
	{
		return cloneIf(super.query(user, query));
	}

	@Override
	public PagingData<Schema> pagingQuery(PagingQuery pagingQuery)
	{
		PagingData<Schema> pagingData = super.pagingQuery(pagingQuery);
		pagingData.setItems(cloneIf(pagingData.getItems()));

		return pagingData;
	}

	@Override
	public PagingData<Schema> pagingQuery(User user, PagingQuery pagingQuery)
	{
		PagingData<Schema> pagingData = super.pagingQuery(user, pagingQuery);
		pagingData.setItems(cloneIf(pagingData.getItems()));

		return pagingData;
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
		deleteCachedSchemaByCreateUserId(oldUserId);

		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("oldUserId", oldUserId);
		params.put("newUserId", newUserId);

		return updateMybatis("updateCreateUserId", params);
	}

	@Override
	public int deleteByUserId(String... userIds)
	{
		deleteCachedSchemaByCreateUserId(userIds);

		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("userIds", userIds);

		return updateMybatis("deleteByUserId", params);
	}

	protected void deleteCachedSchemaByCreateUserId(String... userIds)
	{
		if (this.schemaCache == null)
			return;

		Set<String> cachedIds = this.schemaCache.getAllSchemaIds();

		for (String cachedId : cachedIds)
		{
			Schema schema = this.schemaCache.getSchema(cachedId);

			if (schema != null && schema.getCreateUser() != null)
			{
				String createUserId = schema.getCreateUser().getId();

				for (String userId : userIds)
				{
					if (createUserId.equals(userId))
						this.schemaCache.removeSchema(cachedId);
				}
			}
		}
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

	/**
	 * 如果设置了缓存则拷贝{@linkplain Schema}。
	 * <p>
	 * {@code get...()}方法返回对象修改属性值后不能影响缓存值，所以要进行拷贝。
	 * </p>
	 * 
	 * @param schema
	 * @return
	 */
	protected Schema cloneIf(Schema schema)
	{
		if (schema == null || this.schemaCache == null)
			return schema;
		else
		{
			try
			{
				return schema.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	protected List<Schema> cloneIf(List<Schema> schemas)
	{
		if (schemas == null || this.schemaCache == null)
			return schemas;
		else
		{
			List<Schema> clones = new ArrayList<Schema>(schemas.size());

			try
			{
				for (Schema schema : schemas)
					clones.add(schema.clone());

				return clones;
			}
			catch (CloneNotSupportedException e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
