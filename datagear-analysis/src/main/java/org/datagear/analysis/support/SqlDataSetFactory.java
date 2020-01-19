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
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.support.ParameterSqlResolver.ParameterSql;
import org.datagear.util.JdbcUtil;
import org.datagear.util.JdbcUtil.QueryResultSet;
import org.datagear.util.resource.ConnectionFactory;

/**
 * SQL {@linkplain DataSetFactory}。
 * <p>
 * 它的{@linkplain #setSql(String)}中可以包含<code>${parameter}</code>格式的参数（<code>parameter</code>必须是在{@linkplain #getParams()}中预定义的），
 * 在{@linkplain #getDataSet(DataSetParamValues)}中会被替换为具体的参数值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetFactory extends AbstractDataSetFactory
{
	protected static final SqlDataSetSupport SQL_DATA_SET_SUPPORT = new SqlDataSetSupport();

	private ConnectionFactory connectionFactory;

	private String sql;

	public SqlDataSetFactory()
	{
		super();
	}

	public SqlDataSetFactory(String id, List<DataSetProperty> properties, ConnectionFactory connectionFactory,
			String sql)
	{
		super(id, properties);
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
	public DataSet getDataSet(Map<String, ?> paramValues) throws DataSetException
	{
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
			return getDataSet(cn, this.sql, paramValues);
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

	/**
	 * 获取指定SQL的{@linkplain DataSet}。
	 * 
	 * @param cn
	 * @param sql
	 * @param paramValues
	 * @return
	 */
	protected DataSet getDataSet(Connection cn, String sql, Map<String, ?> paramValues) throws DataSetException
	{
		ParameterSql parameterSql = getSqlDataSetSupport().resolveParameterSql(sql);
		sql = parameterSql.getSql();
		List<DataSetParam> dataSetParams = (parameterSql.hasParameter() ? getParamsNotNull(parameterSql.getParameters())
				: null);

		QueryResultSet qrs = null;

		try
		{
			qrs = getSqlDataSetSupport().buildQueryResultSet(cn, sql, dataSetParams, paramValues);
			return toDataSet(cn, qrs.getResultSet());
		}
		catch (SQLException e)
		{
			throw new DataSetException(e);
		}
		finally
		{
			if (qrs != null)
				qrs.close();
		}
	}

	/**
	 * 将{@linkplain ResultSet}转换为{@linkplain DataSet}。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public DataSet toDataSet(Connection cn, ResultSet rs) throws SQLException
	{
		List<Map<String, ?>> datas = getSqlDataSetSupport().resolveDatas(cn, rs, getProperties());
		MapDataSet dataSet = new MapDataSet(datas);

		Map<String, ?> exportValues = getExportValues(dataSet);

		dataSet.setExportValues(exportValues);

		return dataSet;
	}

	protected SqlDataSetSupport getSqlDataSetSupport()
	{
		return SQL_DATA_SET_SUPPORT;
	}
}
