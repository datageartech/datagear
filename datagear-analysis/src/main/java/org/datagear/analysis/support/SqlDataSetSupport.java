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

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataType;
import org.datagear.analysis.support.ParameterSqlResolver.ParameterSql;
import org.datagear.util.JdbcSupport;
import org.datagear.util.JdbcUtil;
import org.datagear.util.QueryResultSet;
import org.datagear.util.SqlType;

/**
 * SQL数据集支持类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetSupport extends JdbcSupport
{
	protected static final ParameterSqlResolver PARAMETER_SQL_RESOLVER = new ParameterSqlResolver();

	public SqlDataSetSupport()
	{
		super();
	}

	/**
	 * 构建{@linkplain QueryResultSet}。
	 * 
	 * @param cn
	 * @param sql
	 * @param dataSetParams
	 *            允许为{@code null}
	 * @param paramValues
	 *            当{@code dataSetParams}为{@code null}时，允许为{@code null}
	 * @return
	 * @throws SQLException
	 */
	public QueryResultSet buildQueryResultSet(Connection cn, String sql, List<DataSetParam> dataSetParams,
			Map<String, ?> paramValues) throws SQLException
	{
		Statement st = null;
		ResultSet rs = null;

		try
		{
			if (dataSetParams == null || dataSetParams.isEmpty())
			{
				st = createQueryStatement(cn, ResultSet.TYPE_FORWARD_ONLY);
				rs = st.executeQuery(sql);
			}
			else
			{
				PreparedStatement pst = createQueryPreparedStatement(cn, sql, ResultSet.TYPE_FORWARD_ONLY);
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
	public void setPreparedStatementParams(PreparedStatement pst, List<DataSetParam> params, Map<String, ?> paramValues)
			throws SQLException, DataSetException
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
	public void setPreparedStatementParam(PreparedStatement pst, int parameterIndex, DataSetParam dataSetParam,
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
	 * 解析数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param properties
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, ?>> resolveDatas(Connection cn, ResultSet rs, List<DataSetProperty> properties)
			throws SQLException
	{
		List<Map<String, ?>> datas = new ArrayList<>();

		ResultSetMetaData rsMeta = rs.getMetaData();
		int[] rsColumns = resolveResultsetColumns(properties, rsMeta);

		while (rs.next())
		{
			Map<String, Object> row = new HashMap<>();

			for (int i = 0; i < rsColumns.length; i++)
			{
				DataSetProperty property = properties.get(i);
				int rsColumn = rsColumns[i];

				Object value = resolveDataValue(cn, rs, rsColumn, getColumnSqlType(rsMeta, rsColumn),
						property.getType());

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
	public int[] resolveResultsetColumns(List<DataSetProperty> properties, ResultSetMetaData rsMeta)
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
	public Object resolveDataValue(Connection cn, ResultSet rs, int column, SqlType sqlType, DataType dataType)
			throws SQLException
	{
		Object value = null;

		int type = sqlType.getType();

		if (DataType.isString(dataType))
		{
			switch (type)
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
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isBoolean(dataType))
		{
			switch (type)
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
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isInteger(dataType))
		{
			switch (type)
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
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isDecimal(dataType))
		{
			switch (type)
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
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isDate(dataType))
		{
			switch (type)
			{
				case Types.DATE:
				{
					value = rs.getDate(column);
					break;
				}

				default:
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isTime(dataType))
		{
			switch (type)
			{
				case Types.TIME:
				{
					value = rs.getTime(column);
					break;
				}

				default:
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else if (DataType.isTimestamp(dataType))
		{
			switch (type)
			{
				case Types.TIMESTAMP:
				{
					value = rs.getTimestamp(column);
					break;
				}

				default:
					throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
			}
		}
		else
			throw new UnsupportedOperationException();

		return value;
	}

	/**
	 * 解析{@linkplain DataSetProperty}列表。
	 * 
	 * @param cn
	 * @param rs
	 * @param labels
	 *            {@linkplain DataSetProperty#getLabel()}数组，允许为{@code null}或任意长度的数组
	 * @return
	 * @throws SQLException
	 */
	public List<DataSetProperty> resolveDataSetProperties(Connection cn, ResultSet rs, String[] labels)
			throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();

		List<DataSetProperty> properties = new ArrayList<>(columnCount);

		for (int i = 1; i <= columnCount; i++)
		{
			String columnName = getColumnName(metaData, i);
			SqlType sqlType = getColumnSqlType(metaData, i);

			DataType dataType = toDataType(sqlType);

			DataSetProperty property = createDataSetProperty();
			property.setName(columnName);
			property.setType(dataType);

			if (labels != null && labels.length > i - 1)
				property.setLabel(labels[i - 1]);

			properties.add(property);
		}

		return properties;
	}

	/**
	 * 由SQL类型转换为{@linkplain DataType}。
	 * 
	 * @param sqlType
	 * @return
	 * @throws SQLException
	 */
	public DataType toDataType(SqlType sqlType) throws SQLException
	{
		DataType dataType = null;

		int type = sqlType.getType();

		switch (type)
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
				dataType = DataType.DECIMAL;
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
				throw new SqlDataSetUnsupportedSqlTypeException(sqlType);
		}

		return dataType;
	}

	/**
	 * 解析SQL语句参数名列表。
	 * 
	 * @param sql
	 * @return
	 */
	public List<String> resolveParams(String sql)
	{
		return getParameterSqlResolver().resolve(sql);
	}

	/**
	 * 解析{@linkplain ParameterSql}。
	 * 
	 * @param sql
	 * @param paramValues
	 * @return
	 */
	public ParameterSql evalParameterSql(String sql, Map<String, ?> paramValues)
	{
		return getParameterSqlResolver().evaluate(sql, paramValues);
	}

	protected ParameterSqlResolver getParameterSqlResolver()
	{
		return PARAMETER_SQL_RESOLVER;
	}

	protected DataSetProperty createDataSetProperty()
	{
		return new DataSetProperty();
	}
}
