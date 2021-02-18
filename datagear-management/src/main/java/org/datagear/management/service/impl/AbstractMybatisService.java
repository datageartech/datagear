/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.management.domain.User;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.Order;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.persistence.Query;
import org.datagear.util.JdbcUtil;
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
	protected boolean add(T entity)
	{
		return add(entity, buildParamMap());
	}

	/**
	 * 添加。
	 * 
	 * @param entity
	 * @param params
	 */
	protected boolean add(T entity, Map<String, Object> params)
	{
		checkAddInput(entity);

		addIdentifierQuoteParameter(params);
		params.put("entity", entity);

		insertMybatis("insert", params);

		return true;
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
	 * 
	 * @param entity
	 * @param params
	 * @return
	 */
	protected boolean update(T entity, Map<String, Object> params)
	{
		checkUpdateInput(entity);

		addIdentifierQuoteParameter(params);
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
		addIdentifierQuoteParameter(params);
		params.put("obj", obj);

		return (deleteMybatis("delete", params) > 0);
	}

	protected T get(T param)
	{
		return get(param, buildParamMap());
	}

	/**
	 * 获取。
	 * 
	 * @param id
	 * @param params
	 * @return
	 */
	protected T get(T param, Map<String, Object> params)
	{
		addIdentifierQuoteParameter(params);
		params.put("param", param);

		T entity = selectOneMybatis("get", params);
		entity = postProcessSelect(entity);

		return entity;
	}

	/**
	 * 查询。
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
		addIdentifierQuoteParameter(params);
		addQueryaram(params, query);

		List<T> list = selectListMybatis(statement, params);
		postProcessSelects(list);

		return list;
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
		addIdentifierQuoteParameter(params);

		List<T> list = selectListMybatis(statement, params);
		postProcessSelects(list);

		return list;
	}

	/**
	 * 分页查询。
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
	 * 此方法要求已定义{@code [statement]Count} SQL Mapper。例如：
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
		addIdentifierQuoteParameter(params);
		addQueryaram(params, pagingQuery);

		int total = (Integer) selectOneMybatis(statement + "Count", params);

		PagingData<T> pagingData = new PagingData<>(pagingQuery.getPage(), total, pagingQuery.getPageSize());

		int startIndex = pagingData.getStartIndex();

		addPagingQueryParams(params, startIndex, pagingData.getPageSize());

		List<T> list = null;

		if (this.dialect.supportsPaging())
		{
			list = selectListMybatis(statement, params);
		}
		else
		{
			list = selectListMybatis(statement, params, new RowBounds(startIndex, pagingData.getPageSize()));
		}

		postProcessSelects(list);

		pagingData.setItems(list);

		return pagingData;
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
	protected void addPagingQueryParams(Map<String, Object> params, int startIndex, int fetchSize)
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
	 * 后置处理查询结果列表。
	 * <p>
	 * 此方法对每一个元素调用{@linkplain #postProcessSelect(Object)}。
	 * </p>
	 * 
	 * @param list
	 */
	protected void postProcessSelects(List<T> list)
	{
		if (list == null)
			return;

		for (int i = 0; i < list.size(); i++)
		{
			T ele = list.get(i);
			ele = postProcessSelect(ele);
			list.set(i, ele);
		}
	}

	/**
	 * 后置处理读取结果。
	 * <p>
	 * 默认为空方法，子类可以重写，已实现特定的查询结果处理逻辑。
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	protected T postProcessSelect(T obj)
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
	protected void addQueryaram(Map<String, Object> param, Query query)
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

	/**
	 * 查询一个。
	 * 
	 * @param statement
	 * @return
	 */
	protected <TT> TT selectOneMybatis(String statement)
	{
		SqlSession sqlSession = getSqlSession();

		return sqlSession.selectOne(toGlobalSqlId(statement));
	}

	/**
	 * 查询一个。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected <TT> TT selectOneMybatis(String statement, Object parameter)
	{
		SqlSession sqlSession = getSqlSession();

		return sqlSession.selectOne(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement)
	{
		SqlSession sqlSession = getSqlSession();

		return sqlSession.selectList(toGlobalSqlId(statement));
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement, Object parameter)
	{
		SqlSession sqlSession = getSqlSession();

		return sqlSession.selectList(toGlobalSqlId(statement), parameter);
	}

	/**
	 * 查询列表。
	 * 
	 * @param statement
	 * @param parameter
	 * @param rowBounds
	 * @return
	 */
	protected <E> List<E> selectListMybatis(String statement, Object parameter, RowBounds rowBounds)
	{
		SqlSession sqlSession = getSqlSession();

		return sqlSession.selectList(toGlobalSqlId(statement), parameter, rowBounds);
	}

	/**
	 * 插入。
	 * 
	 * @param statement
	 * @return
	 */
	protected int insertMybatis(String statement)
	{
		return getSqlSession().insert(toGlobalSqlId(statement));
	}

	/**
	 * 插入。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int insertMybatis(String statement, Object parameter)
	{
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
		return getSqlSession().update(toGlobalSqlId(statement));
	}

	/**
	 * 更新。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int updateMybatis(String statement, Object parameter)
	{
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
		return getSqlSession().delete(toGlobalSqlId(statement));
	}

	/**
	 * 删除。
	 * 
	 * @param statement
	 * @param parameter
	 * @return
	 */
	protected int deleteMybatis(String statement, Object parameter)
	{
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
	 * 添加标识符引用符参数。
	 * 
	 * @param params
	 */
	protected void addIdentifierQuoteParameter(Map<String, Object> params)
	{
		params.put(this.identifierQuoteKey, this.dialect.getIdentifierQuote());
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
	 * 获取数据库标识引用符。
	 * <p>
	 * 如果数据库不可用，将返回{@linkplain #CONNECTION_NOT_AVALIABLE}。
	 * </p>
	 * 
	 * @param dataSource
	 * @return
	 */
	protected String getIdentifierQuote(DataSource dataSource)
	{
		String identifierQuote = " ";

		Connection cn = null;

		try
		{
			cn = dataSource.getConnection();
			identifierQuote = cn.getMetaData().getIdentifierQuoteString();
		}
		catch (SQLException e)
		{
		}
		finally
		{
			close(cn);
		}

		return identifierQuote;
	}

	/**
	 * 关闭{@linkplain Connection}。
	 * 
	 * @param cn
	 */
	protected void close(Connection cn)
	{
		JdbcUtil.closeConnection(cn);
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
	 * 构建参数映射表。
	 * 
	 * @return
	 */
	protected Map<String, Object> buildParamMapWithIdentifierQuoteParameter()
	{
		Map<String, Object> map = new HashMap<>();
		addIdentifierQuoteParameter(map);

		return map;
	}

	/**
	 * 转义为SQL字符串值。
	 * 
	 * @param s
	 * @return
	 */
	protected String escapeForSqlStringValue(String s)
	{
		if (s == null)
			throw new IllegalArgumentException();

		StringBuilder sb = new StringBuilder();

		for (int i = 0, len = s.length(); i < len; i++)
		{
			char c = s.charAt(i);

			if (c == '\'')
				sb.append("''");
			else
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * 获取sql语句的名字空间。
	 * 
	 * @return
	 */
	protected abstract String getSqlNamespace();
}
