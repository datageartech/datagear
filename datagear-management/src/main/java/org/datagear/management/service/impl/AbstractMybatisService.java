/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.User;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;

/**
 * 抽象基于Mybatis的服务类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractMybatisService<T> extends SqlSessionDaoSupport
{
	public static final String DEFAULT_IDENTIFIER_QUOTE_KEY = "_iq_";

	/** 查询参数：不匹配 */
	public static final String QUERY_PARAM_NOT_LIKE = "queryNotLike";

	/** 查询参数：关键字 */
	public static final String QUERY_PARAM_KEYWORD = "queryKeyword";

	/** 查询参数：条件 */
	public static final String QUERY_PARAM_CONDITION = "queryCondition";

	/** 查询参数：排序 */
	public static final String QUERY_PARAM_ORDER = "queryOrder";

	/** 分页查询是否支持 */
	public static final String PAGING_QUERY_SUPPORTED = "_pagingQuerySupported";

	/** 分页查询SQL首部片段 */
	public static final String PAGING_QUERY_HEAD_SQL = "_pagingQueryHead";

	/** 分页查询SQL尾部片段 */
	public static final String PAGING_QUERY_FOOT_SQL = "_pagingQueryFoot";

	/** {@linkplain MbSqlDialect#funcNameMax()}的MyBatis参数名 */
	public static final String FUNC_NAME_MAX = "_FUNC_MAX";

	/** {@linkplain MbSqlDialect#funcNameModInt()}的MyBatis参数名 */
	public static final String FUNC_NAME_MODINT = "_FUNC_MODINT";

	private MbSqlDialect dialect;

	private String identifierQuoteKey = DEFAULT_IDENTIFIER_QUOTE_KEY;

	public AbstractMybatisService()
	{
		super();
	}

	public AbstractMybatisService(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect)
	{
		super();
		setSqlSessionFactory(sqlSessionFactory);
		this.dialect = dialect;
	}

	public AbstractMybatisService(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect)
	{
		super();
		setSqlSessionTemplate(sqlSessionTemplate);
		this.dialect = dialect;
	}

	public MbSqlDialect getDialect()
	{
		return dialect;
	}

	public void setDialect(MbSqlDialect dialect)
	{
		this.dialect = dialect;
	}

	public String getIdentifierQuoteKey()
	{
		return identifierQuoteKey;
	}

	public void setIdentifierQuoteKey(String identifierQuoteKey)
	{
		this.identifierQuoteKey = identifierQuoteKey;
	}

	/**
	 * 添加。
	 * 
	 * @param entity
	 */
	protected void add(T entity)
	{
		add(entity, buildParamMap());
	}

	/**
	 * 添加。
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
		checkUpdateInput(entity);

		params.put("entity", entity);

		return (updateMybatis("update", params) > 0);
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

	protected T get(T param)
	{
		T obj = get(param, buildParamMap());

		if (obj != null)
			obj = postProcessGet(obj);

		return obj;
	}

	/**
	 * 获取。
	 * <p>
	 * 此方法调用底层的{@code get} SQL。
	 * </p>
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T get(T param, Map<String, Object> params)
	{
		params.put("param", param);
		return selectOneMybatis("get", params);
	}

	/**
	 * 查询。
	 * <p>
	 * 此方法调用底层的{@code query} SQL。
	 * </p>
	 * 
	 * @param query
	 * @return
	 */
	protected List<T> query(Query query)
	{
		return query(query, buildParamMap());
	}

	/**
	 * 查询。
	 * <p>
	 * 此方法调用底层的{@code query} SQL。
	 * </p>
	 * 
	 * @param query
	 * @param params
	 * @return
	 */
	protected List<T> query(Query query, Map<String, Object> params)
	{
		return query("query", query, params);
	}

	/**
	 * 查询。
	 * 
	 * @param statement
	 * @param query
	 * @param params
	 * @return
	 */
	protected List<T> query(String statement, Query query, Map<String, Object> params)
	{
		addQueryParam(params, query);

		List<T> list = query(statement, params);

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
	 * @return
	 */
	protected PagingData<T> pagingQuery(PagingQuery pagingQuery)
	{
		return pagingQuery(pagingQuery, buildParamMap());
	}

	/**
	 * 分页查询。
	 * <p>
	 * 此方法调用底层的{@code pagingQuery}、{@code pagingQueryCount} SQL。
	 * </p>
	 * 
	 * @param pagingQuery
	 * @param params
	 * @return
	 */
	protected PagingData<T> pagingQuery(PagingQuery pagingQuery, Map<String, Object> params)
	{
		return pagingQuery("pagingQuery", pagingQuery, params);
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
	 * @return
	 */
	protected PagingData<T> pagingQuery(String statement, PagingQuery pagingQuery, Map<String, Object> params)
	{
		addQueryParam(params, pagingQuery);

		int total = (Integer) selectOneMybatis(statement + "Count", params);

		PagingData<T> pagingData = new PagingData<>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		int startIndex = pagingData.getStartIndex();

		addDialectParamsPagingQuery(params, startIndex, pagingData.getPageSize());

		List<T> list = null;

		if (this.dialect.supportsPaging())
			list = query(statement, params);
		else
			list = query(statement, params, new RowBounds(startIndex, pagingData.getPageSize()));

		postProcessQuery(list);

		pagingData.setItems(list);

		return pagingData;
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
	 * 后置处理查询结果列。
	 * <p>
	 * {@linkplain #query(Query)}、{@linkplain #query(Query, Map)}、{@linkplain #query(String, Query, Map)}、
	 * {@linkplain #pagingQuery(PagingQuery)}、{@linkplain #pagingQuery(PagingQuery, Map)}、{@linkplain #pagingQuery(String, PagingQuery, Map)}
	 * 内部会调用此方法。
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
	 */
	protected void postProcessQuery(List<T> list)
	{
	}

	/**
	 * 后置处理获取操作结果。
	 * <p>
	 * {@linkplain #get(Object, Map, boolean)}的{@code postProcessGet}为{@code true}时，其内部会调用此方法。
	 * </p>
	 * <p>
	 * 子类可以重写此方法，已实现特定的获取操作结果处理逻辑。
	 * </p>
	 * <p>
	 * 此方法的默认实现是：直接返回{@code obj}。
	 * </p>
	 * 
	 * @param obj
	 *            不会为{@code null}
	 * @return
	 */
	protected T postProcessGet(T obj)
	{
		return obj;
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
	 * 添加{@linkplain Query}参数。
	 * 
	 * @param param
	 * @param query
	 * @return
	 */
	protected void addQueryParam(Map<String, Object> param, Query query)
	{
		String keyword = query.getKeyword();
		String condition = query.getCondition();
		Order[] orders = query.getOrders();

		param.put(QUERY_PARAM_NOT_LIKE, query.isNotLike());

		if (keyword != null && !keyword.isEmpty())
		{
			if (!keyword.startsWith("%") && !keyword.endsWith("%"))
				keyword = "%" + keyword + "%";

			param.put(QUERY_PARAM_KEYWORD, keyword);
		}

		if (condition != null && !condition.isEmpty())
		{
			param.put(QUERY_PARAM_CONDITION, condition);
		}

		if (orders != null && orders.length > 0)
		{
			StringBuilder orderSql = new StringBuilder();

			for (Order order : orders)
			{
				if (orderSql.length() > 0)
					orderSql.append(", ");

				orderSql.append(toQuoteIdentifier(order.getName()));
				orderSql.append(" ");

				if ("DESC".equalsIgnoreCase(order.getType()))
					orderSql.append("DESC");
				else
					orderSql.append("ASC");
			}

			param.put(QUERY_PARAM_ORDER, orderSql.toString());
		}
	}

	protected void addDialectParamsBase(Map<String, Object> param)
	{
		param.put(this.identifierQuoteKey, this.dialect.getIdentifierQuote());
		param.put(FUNC_NAME_MAX, this.dialect.funcNameMax());
		param.put(FUNC_NAME_MODINT, this.dialect.funcNameModInt());
	}

	/**
	 * 添加分页查询参数。
	 * 
	 * @param params
	 * @param startIndex
	 *            起始索引，以{@code 0}开始
	 * @param fetchSize
	 *            页大小
	 */
	protected void addDialectParamsPagingQuery(Map<String, Object> params, int startIndex, int fetchSize)
	{
		params.put(PAGING_QUERY_SUPPORTED, this.dialect.supportsPaging());

		String sqlHead = null;
		String sqlFoot = null;

		if (this.dialect.supportsPaging())
		{
			sqlHead = this.dialect.pagingSqlHead(startIndex, fetchSize);
			sqlFoot = this.dialect.pagingSqlFoot(startIndex, fetchSize);
		}
		else
		{
			// 不支持的话，设为空字符串，方便底层SQL Mapper处理

			sqlHead = "";
			sqlFoot = "";
		}

		params.put(PAGING_QUERY_HEAD_SQL, sqlHead);
		params.put(PAGING_QUERY_FOOT_SQL, sqlFoot);
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
		addDialectParamsBase(parameter);

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
		addDialectParamsBase(parameter);

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
		addDialectParamsBase(parameter);

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
		addDialectParamsBase(parameter);

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
		addDialectParamsBase(parameter);

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
		addDialectParamsBase(parameter);

		return getSqlSession().delete(toGlobalSqlId(statement), parameter);
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
