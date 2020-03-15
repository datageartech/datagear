/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.datagear.util.JdbcUtil;

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
	 */
	public void close()
	{
		JdbcUtil.closeResultSet(this.resultSet);
		JdbcUtil.closeStatement(this.statement);
	}

	public static void close(QueryResultSet qrs)
	{
		if (qrs != null)
			qrs.close();
	}
}
