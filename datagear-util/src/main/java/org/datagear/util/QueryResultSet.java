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

package org.datagear.util;

import java.io.Closeable;
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
public class QueryResultSet implements Closeable
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
	@Override
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
