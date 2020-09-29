/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.Entity;
import org.datagear.management.service.EntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * 抽象基于Mybatis的{@linkplain EntityService}实现类。
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
	public boolean deleteById(ID id)
	{
		return deleteById(id, buildParamMap());
	}

	@Override
	public boolean[] deleteByIds(ID[] ids)
	{
		boolean[] re = new boolean[ids.length];

		for (int i = 0; i < ids.length; i++)
			re[i] = deleteById(ids[i]);

		return re;
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

	/**
	 * 获取。
	 * <p>
	 * 此方法内部会执行{@linkplain #postProcessSelect(Object)}。
	 * </p>
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T getById(ID id, Map<String, Object> params)
	{
		return getById(id, params, true);
	}

	/**
	 * 获取。
	 * 
	 * @param id
	 * @param params
	 * @param postProcessSelect
	 *            是否内部执行{@linkplain #postProcessSelect(Object)}
	 * @return
	 */
	protected T getById(ID id, Map<String, Object> params, boolean postProcessSelect)
	{
		addIdentifierQuoteParameter(params);
		params.put("id", id);

		T entity = selectOneMybatis("getById", params);

		if (postProcessSelect)
			entity = postProcessSelect(entity);

		return entity;
	}

	@Override
	public List<T> query(Query query)
	{
		return super.query(query);
	}

	@Override
	public PagingData<T> pagingQuery(PagingQuery pagingQuery)
	{
		return super.pagingQuery(pagingQuery);
	}
}
