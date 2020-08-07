/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;
import org.datagear.util.resource.ConnectionFactory;

/**
 * SQL {@linkplain DataSet}。
 * <p>
 * 此类的{@linkplain #getSql()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSet extends AbstractFmkTemplateDataSet
{
	protected static final SqlDataSetSupport SQL_DATA_SET_SUPPORT = new SqlDataSetSupport();

	private ConnectionFactory connectionFactory;

	private String sql;

	public SqlDataSet()
	{
		super();
	}

	public SqlDataSet(String id, String name, List<DataSetProperty> properties, ConnectionFactory connectionFactory,
			String sql)
	{
		super(id, name, properties);
		this.connectionFactory = connectionFactory;
		this.sql = sql;
	}

	public ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		String sql = resolveTemplate(getSql(), paramValues);

		Connection cn = null;

		try
		{
			cn = getConnectionFactory().get();
		}
		catch (Exception e)
		{
			JdbcUtil.closeConnection(cn);
			throw new SqlDataSetConnectionException(e);
		}

		try
		{
			return getResult(cn, sql);
		}
		finally
		{
			try
			{
				getConnectionFactory().release(cn);
			}
			catch (Exception e)
			{
			}
		}
	}

	protected DataSetResult getResult(Connection cn, String sql) throws DataSetException
	{
		Sql sqlObj = Sql.valueOf(sql);

		QueryResultSet qrs = null;

		try
		{
			qrs = getSqlDataSetSupport().executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
			return toDataSetResult(cn, qrs.getResultSet());
		}
		catch (SQLException e)
		{
			throw new SqlDataSetSqlExecutionException(sql, e);
		}
		finally
		{
			QueryResultSet.close(qrs);
		}
	}

	public DataSetResult toDataSetResult(Connection cn, ResultSet rs) throws SQLException
	{
		List<Map<String, ?>> datas = getSqlDataSetSupport().resolveResultDatas(cn, rs, getProperties());
		DataSetResult result = new DataSetResult(datas);

		return result;
	}

	protected SqlDataSetSupport getSqlDataSetSupport()
	{
		return SQL_DATA_SET_SUPPORT;
	}
}
