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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.ParameterSqlResolver.ParameterSql;
import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;
import org.datagear.util.resource.ConnectionFactory;

/**
 * SQL {@linkplain DataSet}。
 * <p>
 * 它的{@linkplain #setSql(String)}中可以包含<code>#{parameter}</code>格式的参数（<code>parameter</code>必须是在{@linkplain #getParams()}中预定义的），
 * 在{@linkplain #getResult(Map)}中会被替换为具体的参数值。
 * </p>
 * <p>
 * 它的{@linkplain DataSetParam#getDefaultValue()}值、{@linkplain #getResult(Map)}方法参数的映射值可以是{@linkplain ParameterSqlResolver#isLiteralParamValue(String)
 * 字面参数值}，具体参考{@linkplain ParameterSqlResolver#evaluate(String, Map)}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSet extends AbstractDataSet
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
	 * 获取指定SQL的{@linkplain DataSetResult}。
	 * 
	 * @param cn
	 * @param sql
	 * @param paramValues
	 * @return
	 */
	protected DataSetResult getDataSet(Connection cn, String sql, Map<String, ?> paramValues) throws DataSetException
	{
		List<String> paramNames = getSqlDataSetSupport().resolveParams(sql);
		List<DataSetParam> dataSetParams = null;

		if (!paramNames.isEmpty())
		{
			HashMap<String, Object> myParamValues = new HashMap<>();
			if (paramValues != null)
				myParamValues.putAll(paramValues);

			dataSetParams = getDataSetParamsNotNull(paramNames);
			for (DataSetParam dataSetParam : dataSetParams)
			{
				if (!myParamValues.containsKey(dataSetParam.getName()) && dataSetParam.hasDefaultValue())
					myParamValues.put(dataSetParam.getName(), dataSetParam.getDefaultValue());
			}

			ParameterSql parameterSql = getSqlDataSetSupport().evalParameterSql(sql, myParamValues);

			sql = parameterSql.getSql();
			paramNames = parameterSql.getParameterNames();
			dataSetParams = getDataSetParamsNotNull(paramNames);
			parameterSql.addParameterValues(myParamValues);
			paramValues = myParamValues;
		}

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
	 * 将{@linkplain ResultSet}转换为{@linkplain DataSetResult}。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public DataSetResult toDataSet(Connection cn, ResultSet rs) throws SQLException
	{
		List<Map<String, ?>> datas = getSqlDataSetSupport().resolveDatas(cn, rs, getProperties());
		MapDataSetResult result = new MapDataSetResult(datas);

		Map<String, ?> exportValues = getExportValues(result);

		result.setExportValues(exportValues);

		return result;
	}

	protected SqlDataSetSupport getSqlDataSetSupport()
	{
		return SQL_DATA_SET_SUPPORT;
	}
}
