/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * 查询结果集。
 * 
 * @author datagear@163.com
 *
 */
public class QueryResultSet
{
	private Statement statement;

	private ResultSet resultSet;

	private List<Object> params;

	public QueryResultSet()
	{
		super();
	}

	public QueryResultSet(Statement statement, ResultSet resultSet)
	{
		super();
		this.statement = statement;
		this.resultSet = resultSet;
	}

	public QueryResultSet(Statement statement, ResultSet resultSet, List<Object> params)
	{
		super();
		this.statement = statement;
		this.resultSet = resultSet;
		this.params = params;
	}

	public Statement getStatement()
	{
		return statement;
	}

	public void setStatement(Statement statement)
	{
		this.statement = statement;
	}

	public ResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	public boolean hasParam()
	{
		return (this.params != null && !this.params.isEmpty());
	}

	public List<Object> getParams()
	{
		return params;
	}

	public void setParams(List<Object> params)
	{
		this.params = params;
	}

	public boolean isPreparedStatement()
	{
		return (this.statement instanceof PreparedStatement);
	}

	public PreparedStatement getPreparedStatement()
	{
		return (PreparedStatement) this.statement;
	}

	/**
	 * 关闭。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
	 */
	public void close()
	{
		if (this.hasParam())
			IOUtil.closeIf(this.params);

		JdbcUtil.closeResultSet(this.resultSet);
		JdbcUtil.closeStatement(this.statement);
	}

	/**
	 * 关闭。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
	 */
	public static void close(QueryResultSet qrs)
	{
		if (qrs != null)
			qrs.close();
	}
}
