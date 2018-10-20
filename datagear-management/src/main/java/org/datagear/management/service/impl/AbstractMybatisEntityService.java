/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.User;
import org.datagear.management.service.EntityService;
import org.datagear.model.support.Entity;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * 抽象基于Mybatis的服务类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisEntityService<ID, T extends Entity<ID>> extends AbstractMybatisService<T>
		implements EntityService<ID, T>
{
	public AbstractMybatisEntityService()
	{
		super();
	}

	public AbstractMybatisEntityService(SqlSessionFactory sqlSessionFactory)
	{
		super(sqlSessionFactory);
	}

	public AbstractMybatisEntityService(SqlSessionTemplate sqlSessionTemplate)
	{
		super(sqlSessionTemplate);
	}

	@Override
	public boolean add(T entity)
	{
		return super.add(entity);
	}

	@Override
	public boolean update(T entity)
	{
		return super.update(entity);
	}

	@Override
	public boolean update(User user, T entity)
	{
		return super.update(user, entity);
	}

	@Override
	public boolean deleteById(ID id)
	{
		return deleteById(id, buildParamMap());
	}

	@Override
	public boolean deleteById(User user, ID id)
	{
		Map<String, Object> params = buildParamMap();
		addOperatorParameter(params, user);

		return deleteById(id, params);
	}

	/**
	 * 删除。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected boolean deleteById(ID id, Map<String, Object> params)
	{
		addIdentifierQuoteParameter(params);
		params.put("id", id);

		return (deleteMybatis("deleteById", params) > 0);
	}

	@Override
	public T getById(ID id)
	{
		return getById(id, buildParamMap());
	}

	@Override
	public T getById(User user, ID id)
	{
		Map<String, Object> params = buildParamMap();
		addOperatorParameter(params, user);

		return getById(id, params);
	}

	/**
	 * 获取。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T getById(ID id, Map<String, Object> params)
	{
		addIdentifierQuoteParameter(params);
		params.put("id", id);

		T entity = selectOneMybatis("getById", params);

		return entity;
	}

	@Override
	public List<T> query(Query query)
	{
		return super.query(query);
	}

	@Override
	public List<T> query(User user, Query query)
	{
		return super.query(user, query);
	}

	@Override
	public PagingData<T> pagingQuery(PagingQuery pagingQuery)
	{
		return super.pagingQuery(pagingQuery);
	}

	@Override
	public PagingData<T> pagingQuery(User user, PagingQuery pagingQuery)
	{
		return super.pagingQuery(user, pagingQuery);
	}
}
