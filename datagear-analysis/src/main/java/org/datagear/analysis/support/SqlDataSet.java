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
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
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
public class SqlDataSet extends AbstractFmkTemplateDataSet implements ResolvableDataSet
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
		List<DataSetProperty> properties = getProperties();

		if (properties == null || properties.isEmpty())
			throw new DataSetException("[getProperties()] must not be empty");

		ResolvedDataSetResult result = getResolvedDataSetResult(paramValues, properties);
		return result.getResult();
	}

	@Override
	public ResolvedDataSetResult resolve(Map<String, ?> paramValues) throws DataSetException
	{
		return getResolvedDataSetResult(paramValues, null);
	}

	/**
	 * 
	 * @param paramValues
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult getResolvedDataSetResult(Map<String, ?> paramValues,
			List<DataSetProperty> properties) throws DataSetException
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

		Sql sqlObj = Sql.valueOf(sql);

		QueryResultSet qrs = null;

		try
		{
			qrs = getSqlDataSetSupport().executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
			ResultSet rs = qrs.getResultSet();

			if (properties == null || properties.isEmpty())
				properties = getSqlDataSetSupport().resolveDataSetProperties(cn, rs, null);

			List<Map<String, ?>> data = getSqlDataSetSupport().resolveResultData(cn, rs, properties);
			DataSetResult result = new DataSetResult(data);

			return new ResolvedDataSetResult(result, properties);
		}
		catch (SQLException e)
		{
			throw new SqlDataSetSqlExecutionException(sql, e);
		}
		finally
		{
			try
			{
				QueryResultSet.close(qrs);
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
	}

	/**
	 * 
	 * @param cn
	 * @param sql
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @return
	 * @throws DataSetException
	 */
	protected ResolvedDataSetResult getResolvedDataSetResult(Connection cn, String sql,
			List<DataSetProperty> properties) throws DataSetException
	{
		Sql sqlObj = Sql.valueOf(sql);

		QueryResultSet qrs = null;

		try
		{
			qrs = getSqlDataSetSupport().executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
			ResultSet rs = qrs.getResultSet();

			if (properties == null || properties.isEmpty())
				properties = getSqlDataSetSupport().resolveDataSetProperties(cn, rs, null);

			List<Map<String, ?>> data = getSqlDataSetSupport().resolveResultData(cn, rs, properties);
			DataSetResult result = new DataSetResult(data);

			return new ResolvedDataSetResult(result, properties);
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

	protected SqlDataSetSupport getSqlDataSetSupport()
	{
		return SQL_DATA_SET_SUPPORT;
	}
}
