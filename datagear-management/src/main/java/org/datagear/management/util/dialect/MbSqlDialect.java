/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.util.dialect;

import java.util.Map;
import java.util.Properties;

import org.datagear.management.service.impl.AbstractMybatisService;
import org.datagear.persistence.Order;
import org.datagear.persistence.Query;
import org.datagear.util.StringUtil;

/**
 * Mybatis SQL方言。
 * <p>
 * 此类仅用于为{@linkplain AbstractMybatisService}及其实现类提供多数据库部署支持。
 * </p>
 * <p>
 * 基本思路是：根据当前部署数据库，生成底层Mybatis所需的数据库方言SQL片段，然后以参数的方式传入底层SQL
 * Mapper语境，组装成合规的SQL语句。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class MbSqlDialect
{
	/**
	 * 变量名：标识符引用符
	 */
	public static final String VAR_IDENTIFIER_QUOTE_KEY = "_iq_";

	/**
	 * 变量名：查询不匹配
	 */
	public static final String VAR_QUERY_NOT_LIKE = "queryNotLike";

	/**
	 * 变量名：查询关键字
	 */
	public static final String VAR_QUERY_KEYWORD = "queryKeyword";

	/**
	 * 变量名：查询条件
	 */
	public static final String VAR_QUERY_CONDITION = "queryCondition";

	/**
	 * 变量名：查询排序
	 */
	public static final String VAR_QUERY_ORDER = "queryOrder";

	/**
	 * 变量名：分页查询是否支持
	 */
	public static final String VAR_PAGING_QUERY_SUPPORTED = "_pagingQuerySupported";

	/**
	 * 变量名：分页查询SQL首部片段
	 */
	public static final String VAR_PAGING_QUERY_HEAD_SQL = "_pagingQueryHead";

	/**
	 * 变量名：分页查询SQL尾部片段
	 */
	public static final String VAR_PAGING_QUERY_FOOT_SQL = "_pagingQueryFoot";

	/** 标识符引用符 */
	private String identifierQuote;

	private MbGlobalVariable globalVariable = new MbGlobalVariable();

	public MbSqlDialect()
	{
	}

	public MbSqlDialect(String identifierQuote)
	{
		super();
		this.identifierQuote = identifierQuote;
	}

	public String getIdentifierQuote()
	{
		return identifierQuote;
	}

	public void setIdentifierQuote(String identifierQuote)
	{
		this.identifierQuote = identifierQuote;
	}

	public MbGlobalVariable getGlobalVariable()
	{
		return globalVariable;
	}

	public void setGlobalVariable(MbGlobalVariable globalVariable)
	{
		this.globalVariable = globalVariable;
	}

	/**
	 * 为标识符添加引用符。
	 * 
	 * @param identifier
	 * @return
	 */
	public String quote(String identifier)
	{
		String iq = getIdentifierQuote();
		return iq + identifier + iq;
	}
	/**
	 * 获取要传递给Mybatis的全局变量。
	 * <p>
	 * 这些变量应被添加至{@linkplain org.apache.ibatis.session.Configuration#setVariables(Properties)}。
	 * </p>
	 * 
	 * @return
	 */
	public Properties getGlobalVariables()
	{
		Properties properties = new Properties();

		properties.put(VAR_IDENTIFIER_QUOTE_KEY, getIdentifierQuote());
		this.globalVariable.inflate(properties);

		return properties;
	}

	/**
	 * 设置{@linkplain Query}查询SQL参数。
	 * 
	 * @param param
	 * @param query
	 * @return
	 */
	public void setQueryParams(Map<String, Object> param, Query query)
	{
		setQueryKeywordParam(param, query);
		setQueryConditionParam(param, query);
		setQueryOrderParam(param, query);
		param.put(VAR_QUERY_NOT_LIKE, query.isNotLike());
	}

	/**
	 * 设置查询关键字参数（{@linkplain Query#getKeyword()}）。
	 * 
	 * @param params
	 * @param query
	 */
	protected void setQueryKeywordParam(Map<String, Object> params, Query query)
	{
		String keyword = query.getKeyword();

		if (StringUtil.isEmpty(keyword))
			keyword = null;
		else
		{
			if (!keyword.startsWith("%") && !keyword.endsWith("%"))
				keyword = "%" + keyword + "%";
		}

		params.put(VAR_QUERY_KEYWORD, keyword);
	}

	/**
	 * 设置查询排序参数（{@linkplain Query#getOrders()}）。
	 * 
	 * @param params
	 * @param query
	 */
	protected void setQueryOrderParam(Map<String, Object> params, Query query)
	{
		String orderSql = null;

		Order[] orders = query.getOrders();

		if (orders != null && orders.length > 0)
		{
			StringBuilder sb = new StringBuilder();

			for (Order order : orders)
			{
				if (sb.length() > 0)
					sb.append(", ");

				sb.append(quote(order.getName()));
				sb.append(" ");

				if ("DESC".equalsIgnoreCase(order.getType()))
					sb.append("DESC");
				else
					sb.append("ASC");
			}

			orderSql = sb.toString();
		}

		params.put(VAR_QUERY_ORDER, orderSql);
	}

	/**
	 * 设置查询条件参数（{@linkplain Query#getCondition()}）。
	 * 
	 * @param params
	 * @param query
	 */
	protected void setQueryConditionParam(Map<String, Object> params, Query query)
	{
		// 禁用查询条件参数，避免SQL注入问题
	}

	/**
	 * 设置分页查询参数。
	 * 
	 * @param sqlParams
	 * @param startIndex
	 *            起始索引，以{@code 0}开始
	 * @param fetchSize
	 *            页大小
	 */
	public void setPagingQueryParams(Map<String, Object> sqlParams, int startIndex, int fetchSize)
	{
		boolean supports = supportsPaging();

		sqlParams.put(VAR_PAGING_QUERY_SUPPORTED, supports);

		String sqlHead = null;
		String sqlFoot = null;

		if (supports)
		{
			sqlHead = pagingSqlHead(startIndex, fetchSize);
			sqlFoot = pagingSqlFoot(startIndex, fetchSize);
		}
		else
		{
			// 不支持的话，设为空字符串，方便底层SQL Mapper处理

			sqlHead = "";
			sqlFoot = "";
		}

		sqlParams.put(VAR_PAGING_QUERY_HEAD_SQL, sqlHead);
		sqlParams.put(VAR_PAGING_QUERY_FOOT_SQL, sqlFoot);
	}

	/**
	 * 将字符串转换为SQL字符串字面值。
	 * <p>
	 * 例如：{@code "abc'def"}应转换为{@code "'abc''def'"}
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public String toStringLiteral(String value)
	{
		if (value == null)
			return "NULL";

		StringBuilder sb = new StringBuilder();

		sb.append('\'');

		for (int i = 0, len = value.length(); i < len; i++)
		{
			char c = value.charAt(i);

			if (c == '\'')
				sb.append("''");
			else
				sb.append(c);
		}

		sb.append('\'');

		return sb.toString();
	}

	/**
	 * 是否支持分页查询。
	 * 
	 * @return
	 */
	public abstract boolean supportsPaging();

	/**
	 * 获取分页查询SQL首部片段。
	 * <p>
	 * 分页查询SQL语句的格式规定为：
	 * </p>
	 * <p>
	 * <code>
	 * 分页查询SQL首部片段<br>
	 * 业务查询语句<br>
	 * 分页查询SQL尾部片段
	 * </code>
	 * </p>
	 * <p>
	 * 例如：
	 * </p>
	 * <p>
	 * <code>
	 * <pre>
	 * SELECT PQ.* FROM (     --分页查询SQL首部片段 <br>
	 * SELECT * FROM TABLE_0  --业务查询语句 <br>
	 * ) PQ LIMIT 0, 10       --分页查询SQL尾部片段
	 * </pre>
	 * </code>
	 * </p>
	 * 
	 * @param index
	 *            页起始索引（以{@code 0}开头）
	 * @param fetchSize
	 *            页大小
	 * @return 返回{@code null}表示不支持分页
	 */
	public abstract String pagingSqlHead(int index, int fetchSize);

	/**
	 * 获取分页查询SQL尾部片段。
	 * 
	 * @param index
	 *            页起始索引（以{@code 0}开头）
	 * @param fetchSize
	 *            页大小
	 * @return 返回{@code null}表示不支持分页
	 */
	public abstract String pagingSqlFoot(int index, int fetchSize);

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [identifierQuote=" + getIdentifierQuote() + ", supportsPaging="
				+ supportsPaging() + ", pagingSqlHead=" + pagingSqlHead(0, 10) + ", pagingSqlFoot="
				+ pagingSqlFoot(0, 10) + "]";
	}
}
