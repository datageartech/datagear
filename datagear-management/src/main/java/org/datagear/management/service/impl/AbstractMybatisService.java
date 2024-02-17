/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.management.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.User;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * 抽象基于Mybatis的服务类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisService<T>
{
	private SqlSessionDaoSupportImpl sqlSessionDaoSupportImpl;

	private MbSqlDialect dialect;

	public AbstractMybatisService()
	{
		super();
	}

	public AbstractMybatisService(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super();
		this.sqlSessionDaoSupportImpl = new SqlSessionDaoSupportImpl(sqlSessionFactory);
		this.dialect = dialect;

		this.sqlSessionDaoSupportImpl.afterPropertiesSet();
	}

	public AbstractMybatisService(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super();
		this.sqlSessionDaoSupportImpl = new SqlSessionDaoSupportImpl(sqlSessionTemplate);
		this.dialect = dialect;

		this.sqlSessionDaoSupportImpl.afterPropertiesSet();
	}

	public MbSqlDialect getDialect()
	{
		return dialect;
	}

	public void setDialect(MbSqlDialect dialect)
	{
		this.dialect = dialect;
	}

	protected SqlSessionDaoSupportImpl getSqlSessionDaoSupportImpl()
	{
		return sqlSessionDaoSupportImpl;
	}

	protected void setSqlSessionDaoSupportImpl(SqlSessionDaoSupportImpl sqlSessionDaoSupportImpl)
	{
		this.sqlSessionDaoSupportImpl = sqlSessionDaoSupportImpl;
	}

	/**
	 * 添加。
	 * <p>
	 * 此方法调用底层的{@code insert} SQL。
	 * </p>
	 * 
	 * @param entity
	 */
	protected void add(T entity)
	{
		add(entity, buildParamMap());
	}

	/**
	 * 添加。
	 * <p>
	 * 此方法调用底层的{@code insert} SQL。
	 * </p>
	 * 
	 * @param entity
	 * @param params
	 */
	protected void add(T entity, Map<String, Object> params)
	{
		checkAddInput(entity);

		params.put("entity", entity);

		insertMybatis("insert", params);
	}

	/**
	 * 更新。
	 * <p>
	 * 此方法调用底层的{@code update} SQL。
	 * </p>
	 * 
	 * @param entity
	 * @return
	 */
	protected boolean update(T entity)
	{
		return update(entity, buildParamMap());
	}

	/**
	 * 更新。
	 * <p>
	 * 此方法调用底层的{@code update} SQL。
	 * </p>
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	protected boolean update(T entity, Map<String, Object> params)
	{
		return (update("update", entity, params) > 0);
	}

	/**
	 * 更新。
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	protected int update(String statement, T entity, Map<String, Object> params)
	{
		checkUpdateInput(entity);
		params.put("entity", entity);

		return updateMybatis(statement, params);
	}

	/**
	 * 删除。
	 * 
	 * @param obj
	 * @return
	 */
	protected boolean delete(T obj)
	{
		return delete(obj, buildParamMap());
	}

	/**
	 * 删除。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected boolean delete(T obj, Map<String, Object> params)
	{
		params.put("obj", obj);

		return (deleteMybatis("delete", params) > 0);
	}

	/**
	 * 根据参数获取对象。
	 * <p>
	 * 此方法调用底层的{@code getByParam} SQL。
	 * </p>
	 * 
	 * @param param
	 * @param postProcessGet
	 *            是否内部执行{@linkplain #postProcessGet(Object)}
	 * @return
	 */
	protected T getByParam(T param, boolean postProcessGet)
	{
		T obj = getByParam("getByParam", param, buildParamMap());

		if (postProcessGet)
			obj = postProcessGetNullable(obj);

		return obj;
	}

	/**
	 * 后置处理获取操作结果。
	 * <p>
	 * 如果{@code obj}为{@code null}，将直接返回{@code null}；否则返回{@linkplain #postProcessGet(Object)}。
	 * </p>
	 * <p>
	 * 子类要实现特定的获取操作结果处理逻辑应重写{@linkplain #postProcessGet(Object)}。
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected T postProcessGetNullable(T obj)
	{
		if (obj == null)
			return null;

		return postProcessGet(obj);
	}

	/**
	 * 后置处理获取操作结果。
	 * <p>
	 * 子类可以重写此方法，以实现特定的获取操作结果处理逻辑。
	 * </p>
	 * <p>
	 * 此方法的默认实现是：直接返回原对象。
	 * </p>
	 * 
	 * @param obj
	 *            不会为{@code null}
	 * @return
	 * @see #getByParam(Object)
	 */
	protected T postProcessGet(T obj)
	{
		return obj;
	}

	/**
	 * 查询。
	 * <p>
	 * 此方法调用底层的{@code query} SQL。
	 * </p>
	 * 
	 * @param query
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected List<T> query(Query query, boolean postProcessQuery)
	{
		return query(query, buildParamMap(), postProcessQuery);
	}

	/**
	 * 查询。
	 * <p>
	 * 此方法调用底层的{@code query} SQL。
	 * </p>
	 * 
	 * @param query
	 * @param params
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected List<T> query(Query query, Map<String, Object> params, boolean postProcessQuery)
	{
		return query("query", query, params, postProcessQuery);
	}

	/**
	 * 查询。
	 * <p>
	 * 此方法内部会执行{@linkplain #postProcessQuery(List)}。
	 * </p>
	 * 
	 * @param statement
	 * @param query
	 * @param params
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected List<T> query(String statement, Query query, Map<String, Object> params, boolean postProcessQuery)
	{
		setQueryParams(params, query);

		List<T> list = query(statement, params);

		if (postProcessQuery)
			postProcessQuery(list);

		return list;
	}

	/**
	 * 分页查询。
	 * <p>
	 * 此方法调用底层的{@code pagingQuery}、{@code pagingQueryCount} SQL。
	 * </p>
	 * 
	 * @param pagingQuery
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected PagingData<T> pagingQuery(PagingQuery pagingQuery, boolean postProcessQuery)
	{
		return pagingQuery(pagingQuery, buildParamMap(), postProcessQuery);
	}

	/**
	 * 分页查询。
	 * <p>
	 * 此方法调用底层的{@code pagingQuery}、{@code pagingQueryCount} SQL。
	 * </p>
	 * 
	 * @param pagingQuery
	 * @param params
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected PagingData<T> pagingQuery(PagingQuery pagingQuery, Map<String, Object> params, boolean postProcessQuery)
	{
		return pagingQuery("pagingQuery", pagingQuery, params, postProcessQuery);
	}

	/**
	 * 分页查询。
	 * <p>
	 * 此方法要求已定义{@code [statement]Count} SQL。例如：
	 * </p>
	 * <p>
	 * 如果{@code statement}为{@code "pagingQuery"}，那么必须已定义{@code "pagingQueryCount"}
	 * SQL Mapper。
	 * </p>
	 * 
	 * @param statement
	 * @param pagingQuery
	 * @param params
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected PagingData<T> pagingQuery(String statement, PagingQuery pagingQuery, Map<String, Object> params,
			boolean postProcessQuery)
	{
		return pagingQuery(statement, pagingQuery, params, true, 0, postProcessQuery);
	}

	/**
	 * 分页查询。
	 * <p>
	 * 如果{@code queryTotal}为{@code true}，此方法要求已定义{@code [statement]Count}
	 * SQL。例如：
	 * </p>
	 * <p>
	 * 如果{@code statement}为{@code "pagingQuery"}，那么必须已定义{@code "pagingQueryCount"}
	 * SQL Mapper。
	 * </p>
	 * 
	 * @param statement
	 * @param pagingQuery
	 * @param params
	 * @param queryTotal
	 * @param total
	 *            如果{@code queryTotal}为{@code false}，应设置此总记录数；否则，设为{@code 0}即可
	 * @param postProcessQuery
	 *            是否内部执行{@linkplain #postProcessQuery(List)}
	 * @return
	 */
	protected PagingData<T> pagingQuery(String statement, PagingQuery pagingQuery, Map<String, Object> params,
			boolean queryTotal, int total, boolean postProcessQuery)
	{
		setQueryParams(params, pagingQuery);

		if (queryTotal)
			total = (Integer) selectOneMybatis(statement + "Count", params);

		PagingData<T> pagingData = new PagingData<>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		int startIndex = pagingData.getStartIndex();

		setPagingQueryParams(params, startIndex, pagingData.getPageSize());

		List<T> list = null;

		if (this.dialect.supportsPaging())
			list = query(statement, params);
		else
			list = query(statement, params, new RowBounds(startIndex, pagingData.getPageSize()));

		if (postProcessQuery)
			postProcessQuery(list);

		pagingData.setItems(list);

		return pagingData;
	}

	/**
	 * 后置处理查询结果列。
	 * </p>
	 * <p>
	 * 子类可以重写此方法，已实现特定的查询结果处理逻辑。
	 * </p>
	 * <p>
	 * 此方法的默认实现是：什么也不做
	 * </p>
	 * 
	 * @param list
	 *            不会为{@code null}
	 * @see #query(Query, boolean)
	 * @see #query(Query, Map, boolean)
	 * @see #query(String, Query, Map, boolean)
	 * @see #pagingQuery(PagingQuery, boolean)
	 * @see #pagingQuery(PagingQuery, Map, boolean)
	 * @see #pagingQuery(String, PagingQuery, Map, boolean)
	 */
	protected void postProcessQuery(List<T> list)
	{
	}

	/**
	 * 获取。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T getByParam(String statement, T param, Map<String, Object> params)
	{
		params.put("param", param);
		return selectOneMybatis(statement, params);
	}

	/**
	 * 查询。
	 * 
	 * @param statement
	 * @param params
	 * @return
	 */
	protected List<T> query(String statement, Map<String, Object> params)
	{
		return selectListMybatis(statement, params);
	}

	/**
	 * 查询。
	 * 
	 * @param statement
	 * @param params
	 * @param rowBounds
	 * @return
	 */
	protected List<T> query(String statement, Map<String, Object> params, RowBounds rowBounds)
	{
		return selectListMybatis(statement, params, rowBounds);
	}

	/**
	 * 校验{@linkplain #add(User, Entity)}的输入参数。
	 * 
	 * @param entity
	 */
	protected void checkAddInput(T entity)
	{
		checkInput(entity);
	}

	/**
	 * 校验{@linkplain #update(User, Entity)}的输入参数。
	 * 
	 * @param entity
	 */
	protected void checkUpdateInput(T entity)
	{
		checkInput(entity);
	}

	/**
	 * 校验{@linkplain #add(User, Entity)}和{@linkplain #update(User, Entity)}的输入。
	 * 
	 * @param entity
	 */
	protected void checkInput(T entity)
	{

	}

	/**
	 * 设置{@linkplain Query}查询SQL参数。
	 * 
	 * @param param
	 * @param query
	 * @return
	 */
	protected void setQueryParams(Map<String, Object> param, Query query)
	{
		this.dialect.setQueryParams(param, query);
	}

	/**
	 * 设置分页查询SQL参数。
	 * 
	 * @param params
	 * @param startIndex
	 *            起始索引，以{@code 0}开始
	 * @param fetchSize
	 *            页大小
	 */
	protected void setPagingQueryParams(Map<String, Object> params, int startIndex, int fetchSize)
	{
		this.dialect.setPagingQueryParams(params, startIndex, fetchSize);
	}

	/**
	 * 查询一个。
	 * 
	 * @param statement
	 * @return
	 */
	protected <TT> TT selectOneMybatis(String statement)
	{
		return selectOneMybatis(statement, buildParamMap());
	}

	/**
	 * 查询一个。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected <TT> TT selectOneMybatis(String statement, Map<String, Object> parameter)
	{
		addBuiltInParams(parameter);

		return getSqlSession().selectOne(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement)
	{
		return selectListMybatis(statement, buildParamMap());
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement, Map<String, Object> parameter)
	{
		addBuiltInParams(parameter);

		return getSqlSession().selectList(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @param parameter
	 * @param rowBounds
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement, Map<String, Object> parameter, RowBounds rowBounds)
	{
		addBuiltInParams(parameter);

		return getSqlSession().selectList(toGlobalSqlId(statement), parameter, rowBounds);
	}

	/**
	 * 插入。
	 * 
	 * @param statement
	 * @return
	 */
	protected int insertMybatis(String statement)
	{
		return insertMybatis(statement, buildParamMap());
	}

	/**
	 * 插入。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int insertMybatis(String statement, Map<String, Object> parameter)
	{
		addBuiltInParams(parameter);

		return getSqlSession().insert(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 更新。
	 * 
	 * @param statement
	 * @return
	 */
	protected int updateMybatis(String statement)
	{
		return updateMybatis(statement, buildParamMap());
	}

	/**
	 * 更新。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int updateMybatis(String statement, Map<String, Object> parameter)
	{
		addBuiltInParams(parameter);

		return getSqlSession().update(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 删除。
	 * 
	 * @param statement
	 * @return
	 */
	protected int deleteMybatis(String statement)
	{
		return deleteMybatis(statement, buildParamMap());
	}

	/**
	 * 删除。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int deleteMybatis(String statement, Map<String, Object> parameter)
	{
		addBuiltInParams(parameter);

		return getSqlSession().delete(toGlobalSqlId(statement), parameter);
	}

	protected SqlSession getSqlSession()
	{
		return getSqlSessionDaoSupportImpl().getSqlSession();
	}

	/**
	 * 添加内置SQL参数。
	 * 
	 * @param param
	 */
	protected void addBuiltInParams(Map<String, Object> param)
	{
	}

	/**
	 * 将局部SQL标识转换为全局SQL标识。
	 * 
	 * @param localSqlId
	 * @return
	 */
	protected String toGlobalSqlId(String localSqlId)
	{
		return getSqlNamespace() + "." + localSqlId;
	}

	/**
	 * 为标识符添加引用符。
	 * 
	 * @param s
	 * @return
	 */
	protected String toQuoteIdentifier(String s)
	{
		String iq = this.dialect.getIdentifierQuote();
		return iq + s + iq;
	}

	/**
	 * 判断对象、字符串、数组、集合、Map是否为空。
	 * 
	 * @param obj
	 * @return
	 */
	protected boolean isEmpty(Object obj)
	{
		return StringUtil.isEmpty(obj);
	}

	/**
	 * 如果元素不为{@code null}，才将其添加至集合。
	 * 
	 * @param <E>
	 * @param collection
	 * @param element
	 * @return
	 */
	protected <E> boolean addIfNonNull(Collection<E> collection, E element)
	{
		if (element == null)
			return false;

		collection.add(element);

		return true;
	}

	/**
	 * 字符串是否为空。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isEmpty(String s)
	{
		return StringUtil.isEmpty(s);
	}

	/**
	 * 字符串是否为空格串。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isBlank(String s)
	{
		return StringUtil.isBlank(s);
	}

	/**
	 * 构建参数映射表。
	 * 
	 * @return
	 */
	protected Map<String, Object> buildParamMap()
	{
		return new HashMap<>();
	}

	/**
	 * 获取sql语句的名字空间。
	 * 
	 * @return
	 */
	protected abstract String getSqlNamespace();
}
