/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataType;
import org.datagear.analysis.support.ParameterSqlResolver.ParameterSql;
import org.datagear.util.JdbcUtil;
import org.datagear.util.JdbcUtil.QueryResultSet;
import org.datagear.util.StringUtil;
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
	protected static final ParameterSqlResolver PARAMETER_SQL_RESOLVER = new ParameterSqlResolver();

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
	 * 获取属性集。
	 * 
	 * @param paramValues
	 * @return
	 * @throws DataSetException
	 */
	public List<DataSetProperty> getProperties(Map<String, ?> paramValues) throws DataSetException
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

		QueryResultSet qrs = null;

		try
		{
			qrs = buildQueryResultSet(cn, sql, paramValues);

			return resolveProperties(cn, qrs.getResultSet());
		}
		catch (SQLException e)
		{
			throw new DataSetException(e);
		}
		finally
		{
			if (qrs != null)
				qrs.close();

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
		QueryResultSet qrs = null;

		try
		{
			qrs = buildQueryResultSet(cn, sql, paramValues);
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
	 * 构建{@linkplain QueryResultSet}。
	 * 
	 * @param cn
	 * @param sql
	 * @param paramValues
	 * @return
	 */
	protected QueryResultSet buildQueryResultSet(Connection cn, String sql, Map<String, ?> paramValues)
			throws SQLException
	{
		ParameterSql parameterSql = resolveParameterSql(sql);

		sql = parameterSql.getSql();
		List<DataSetParam> dataSetParams = (parameterSql.hasParameter() ? getParamsNotNull(parameterSql.getParameters())
				: null);

		Statement st = null;
		ResultSet rs = null;

		try
		{
			if (dataSetParams == null || dataSetParams.isEmpty())
			{
				st = cn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				rs = st.executeQuery(sql);
			}
			else
			{
				PreparedStatement pst = cn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				setPreparedStatementParams(pst, dataSetParams, paramValues);

				st = pst;
				rs = pst.executeQuery();
			}
		}
		catch (SQLException e)
		{
			JdbcUtil.closeResultSet(rs);
			JdbcUtil.closeStatement(st);

			throw e;
		}

		return new QueryResultSet(st, rs);
	}

	/**
	 * 设置参数值。
	 * 
	 * @param pst
	 * @param params
	 * @param paramValues
	 * @throws SQLException
	 */
	protected void setPreparedStatementParams(PreparedStatement pst, List<DataSetParam> params,
			Map<String, ?> paramValues) throws SQLException, DataSetException
	{
		for (int i = 0, len = params.size(); i < len; i++)
		{
			DataSetParam dataSetParam = params.get(i);
			Object value = paramValues.get(dataSetParam.getName());
			if (value == null)
				value = dataSetParam.getDefaultValue();

			if (value == null && dataSetParam.isRequired())
				throw new DataSetParamValueRequiredException(dataSetParam.getName());

			setPreparedStatementParam(pst, i + 1, dataSetParam, value);
		}
	}

	/**
	 * 设置参数值。
	 * 
	 * @param pst
	 * @param parameterIndex
	 * @param dataSetParam
	 * @param paramValue
	 * @throws SQLException
	 * @throws DataSetException
	 */
	protected void setPreparedStatementParam(PreparedStatement pst, int parameterIndex, DataSetParam dataSetParam,
			Object paramValue) throws SQLException, DataSetException
	{
		DataType dataType = dataSetParam.getType();

		if (DataType.isString(dataType))
		{
			String value = null;

			if (paramValue == null)
				;
			else if (paramValue instanceof String)
				value = (String) paramValue;
			else
				throw new DataSetException("Type [" + paramValue.getClass().getName() + "] for [" + DataType.STRING
						+ "] is not supported");

			if (paramValue == null)
				pst.setNull(parameterIndex, Types.VARCHAR);
			else
				pst.setString(parameterIndex, value);
		}
		else if (DataType.isBoolean(dataType))
		{
			boolean value = false;

			if (paramValue == null)
				;
			else if (paramValue instanceof Boolean)
				value = ((Boolean) paramValue).booleanValue();
			else
				throw new DataSetException("Type [" + paramValue.getClass().getName() + "] for [" + DataType.BOOLEAN
						+ "] is not supported");

			if (paramValue == null)
				pst.setNull(parameterIndex, Types.BOOLEAN);
			else
				pst.setBoolean(parameterIndex, value);
		}
		else if (DataType.isInteger(dataType))
		{
			if (paramValue == null)
				pst.setNull(parameterIndex, Types.INTEGER);
			else if (paramValue instanceof BigInteger)
				pst.setBigDecimal(parameterIndex, new BigDecimal((BigInteger) paramValue));
			else if (paramValue instanceof Long)
				pst.setLong(parameterIndex, (Long) paramValue);
			else if (paramValue instanceof Number)
				pst.setInt(parameterIndex, ((Number) paramValue).intValue());
			else
				throw new DataSetException("Type [" + paramValue.getClass().getName() + "] for [" + DataType.INTEGER
						+ "] is not supported");
		}
		else if (DataType.isDecimal(dataType))
		{
			if (paramValue == null)
				pst.setNull(parameterIndex, Types.DECIMAL);
			else if (paramValue instanceof BigDecimal)
				pst.setBigDecimal(parameterIndex, (BigDecimal) paramValue);
			else if (paramValue instanceof BigInteger)
				pst.setBigDecimal(parameterIndex, new BigDecimal((BigInteger) paramValue));
			else if (paramValue instanceof Double)
				pst.setDouble(parameterIndex, (Double) paramValue);
			else if (paramValue instanceof Float)
				pst.setFloat(parameterIndex, (Float) paramValue);
			else if (paramValue instanceof Number)
				pst.setDouble(parameterIndex, ((Number) paramValue).doubleValue());
			else
				throw new DataSetException("Type [" + paramValue.getClass().getName() + "] for [" + DataType.DECIMAL
						+ "] is not supported");
		}
		else if (DataType.isDate(dataType))
		{
			if (paramValue == null)
				pst.setNull(parameterIndex, Types.DATE);
			else if (paramValue instanceof java.sql.Date)
				pst.setDate(parameterIndex, (java.sql.Date) paramValue);
			else if (paramValue instanceof java.util.Date)
				pst.setDate(parameterIndex, new java.sql.Date(((java.util.Date) paramValue).getTime()));
			else
				throw new DataSetException(
						"Type [" + paramValue.getClass().getName() + "] for [" + DataType.DATE + "] is not supported");
		}
		else if (DataType.isTime(dataType))
		{
			if (paramValue == null)
				pst.setNull(parameterIndex, Types.TIME);
			else if (paramValue instanceof java.sql.Time)
				pst.setTime(parameterIndex, (java.sql.Time) paramValue);
			else if (paramValue instanceof java.util.Date)
				pst.setTime(parameterIndex, new java.sql.Time(((java.util.Date) paramValue).getTime()));
			else
				throw new DataSetException(
						"Type [" + paramValue.getClass().getName() + "] for [" + DataType.TIME + "] is not supported");
		}
		else if (DataType.isTimestamp(dataType))
		{
			if (paramValue == null)
				pst.setNull(parameterIndex, Types.TIMESTAMP);
			else if (paramValue instanceof java.sql.Timestamp)
				pst.setTimestamp(parameterIndex, (java.sql.Timestamp) paramValue);
			else if (paramValue instanceof java.util.Date)
				pst.setTimestamp(parameterIndex, new java.sql.Timestamp(((java.util.Date) paramValue).getTime()));
			else
				throw new DataSetException(
						"Type [" + paramValue.getClass().getName() + "] for [" + DataType.TIME + "] is not supported");
		}
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * 将{@linkplain ResultSet}转换为{@linkplain DataSet}。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected DataSet toDataSet(Connection cn, ResultSet rs) throws SQLException
	{
		List<Map<String, ?>> datas = resolveDatas(cn, rs, getProperties());
		MapDataSet dataSet = new MapDataSet(datas);

		Map<String, ?> exportValues = getExportValues(dataSet);

		dataSet.setExportValues(exportValues);

		return dataSet;
	}

	/**
	 * 解析数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param properties
	 * @return
	 * @throws SQLException
	 */
	protected List<Map<String, ?>> resolveDatas(Connection cn, ResultSet rs, List<DataSetProperty> properties)
			throws SQLException
	{
		List<Map<String, ?>> datas = new ArrayList<Map<String, ?>>();

		ResultSetMetaData rsMeta = rs.getMetaData();
		int[] rsColumns = resolveResultsetColumns(properties, rsMeta);

		while (rs.next())
		{
			Map<String, Object> row = new HashMap<String, Object>();

			for (int i = 0; i < rsColumns.length; i++)
			{
				DataSetProperty property = properties.get(i);
				int rsColumn = rsColumns[i];

				Object value = resolveDataValue(cn, rs, rsColumn, rsMeta.getColumnType(rsColumn), property.getType());

				row.put(property.getName(), value);
			}

			datas.add(row);
		}

		return datas;
	}

	/**
	 * 解析结果集中对应{@linkplain DataSetProperty}的索引数组。
	 * 
	 * @param properties
	 * @param rsMeta
	 * @return
	 * @throws SQLException
	 * @throws DataSetException
	 */
	protected int[] resolveResultsetColumns(List<DataSetProperty> properties, ResultSetMetaData rsMeta)
			throws SQLException, DataSetException
	{
		int[] columns = new int[properties.size()];

		int rsColumnCount = rsMeta.getColumnCount();

		for (int i = 0; i < columns.length; i++)
		{
			String pname = properties.get(i).getName();

			int myIndex = -1;

			for (int j = 1; j <= rsColumnCount; j++)
			{
				if (pname.equalsIgnoreCase(rsMeta.getColumnLabel(j)))
				{
					myIndex = j;
					break;
				}
			}

			if (myIndex <= 0)
				throw new DataSetException(
						"Column named '" + pname + "' not found in the " + ResultSet.class.getSimpleName());

			columns[i] = myIndex;
		}

		return columns;
	}

	/**
	 * 解析数据值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @param sqlType
	 * @param dataType
	 * @return
	 * @throws SQLException
	 */
	protected Object resolveDataValue(Connection cn, ResultSet rs, int column, int sqlType, DataType dataType)
			throws SQLException
	{
		Object value = null;

		if (DataType.isString(dataType))
		{
			switch (sqlType)
			{
				case Types.CHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
				case Types.VARCHAR:
				{
					value = rs.getString(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isBoolean(dataType))
		{
			switch (sqlType)
			{
				case Types.BIT:
				case Types.BIGINT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
				{
					value = (rs.getInt(column) > 0);
					break;
				}

				case Types.BOOLEAN:
				{
					value = rs.getBoolean(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isInteger(dataType))
		{
			switch (sqlType)
			{
				case Types.BIGINT:
				{
					value = rs.getLong(column);
					break;
				}

				case Types.BIT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
				{
					value = rs.getInt(column);
					break;
				}

				case Types.DECIMAL:
				case Types.NUMERIC:
				{
					value = rs.getBigDecimal(column).toBigInteger();
					break;
				}

				case Types.DOUBLE:
				{
					value = new Double(rs.getDouble(column)).longValue();
					break;
				}

				case Types.FLOAT:
				case Types.REAL:
				{
					value = new Float(rs.getFloat(column)).longValue();
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isDecimal(dataType))
		{
			switch (sqlType)
			{
				case Types.BIGINT:
				{
					value = rs.getLong(column);
					break;
				}

				case Types.BIT:
				case Types.INTEGER:
				case Types.SMALLINT:
				case Types.TINYINT:
				{
					value = rs.getInt(column);
					break;
				}

				case Types.DECIMAL:
				case Types.NUMERIC:
				{
					value = rs.getBigDecimal(column);
					break;
				}

				case Types.DOUBLE:
				{
					value = rs.getDouble(column);
					break;
				}

				case Types.FLOAT:
				case Types.REAL:
				{
					value = rs.getFloat(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isDate(dataType))
		{
			switch (sqlType)
			{
				case Types.DATE:
				{
					value = rs.getDate(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isTime(dataType))
		{
			switch (sqlType)
			{
				case Types.TIME:
				{
					value = rs.getTime(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isTimestamp(dataType))
		{
			switch (sqlType)
			{
				case Types.TIMESTAMP:
				{
					value = rs.getTimestamp(column);
					break;
				}

				default:
					throw new UnsupportedSqlTypeException(sqlType);
			}
		}
		else
			throw new UnsupportedOperationException();

		return value;
	}

	protected List<DataSetProperty> resolveProperties(Connection cn, ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();

		List<DataSetProperty> properties = new ArrayList<DataSetProperty>(columnCount);

		for (int i = 1; i <= columnCount; i++)
		{
			String columnName = metaData.getColumnLabel(i);
			if (StringUtil.isEmpty(columnName))
				columnName = metaData.getColumnName(i);

			DataType dataType = toDataType(metaData, i);

			DataSetProperty columnMeta = createDataSetProperty();
			columnMeta.setName(columnName);
			columnMeta.setType(dataType);

			properties.add(columnMeta);
		}

		return properties;
	}

	/**
	 * 由SQL类型转换为{@linkplain DataType}。
	 * 
	 * @param metaData
	 * @param column
	 * @return
	 * @throws SQLException
	 */
	protected DataType toDataType(ResultSetMetaData metaData, int column) throws SQLException
	{
		DataType dataType = null;

		int sqlType = metaData.getColumnType(column);

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.VARCHAR:
			{
				dataType = DataType.STRING;
				break;
			}

			case Types.BOOLEAN:
			{
				dataType = DataType.BOOLEAN;
				break;
			}

			case Types.BIGINT:
			case Types.BIT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
			{
				dataType = DataType.INTEGER;
				break;
			}

			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.NUMERIC:
			case Types.REAL:
			{
				int scale = metaData.getScale(column);

				dataType = (scale > 0 ? DataType.DECIMAL : DataType.INTEGER);
				break;
			}

			case Types.DATE:
			{
				dataType = DataType.DATE;
				break;
			}

			case Types.TIME:
			{
				dataType = DataType.TIME;
				break;
			}

			case Types.TIMESTAMP:
			{
				dataType = DataType.TIMESTAMP;
				break;
			}

			default:
				throw new UnsupportedSqlTypeException(sqlType);
		}

		return dataType;
	}

	protected ParameterSql resolveParameterSql(String sql)
	{
		return PARAMETER_SQL_RESOLVER.resolve(sql);
	}

	protected DataSetProperty createDataSetProperty()
	{
		return new DataSetProperty();
	}
}
