/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetProperty.DataType;
import org.datagear.util.JdbcSupport;
import org.datagear.util.SqlType;

/**
 * SQL数据集支持类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetSupport extends JdbcSupport
{
	public SqlDataSetSupport()
	{
		super();
	}

	/**
	 * 解析结果数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param properties
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, ?>> resolveResultDatas(Connection cn, ResultSet rs, List<DataSetProperty> properties)
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

				Object value = resolvePropertyDataValue(cn, rs, rsColumn, getColumnSqlType(rsMeta, rsColumn),
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
				if (pname.equalsIgnoreCase(getColumnName(rsMeta, j)))
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
	public Object resolvePropertyDataValue(Connection cn, ResultSet rs, int column, SqlType sqlType, String dataType)
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

		if (rs.wasNull())
			value = null;

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

			String dataType = toPropertyDataType(sqlType, columnName);

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
	 * 由SQL类型转换为{@linkplain DataSetProperty#getType()}。
	 * 
	 * @param sqlType
	 * @param columnName
	 *            允许为{@code null}，列名称
	 * @return
	 * @throws SQLException
	 */
	public String toPropertyDataType(SqlType sqlType, String columnName) throws SQLException
	{
		String dataType = null;

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
				throw new SqlDataSetUnsupportedSqlTypeException(sqlType, columnName);
		}

		return dataType;
	}

	protected DataSetProperty createDataSetProperty()
	{
		return new DataSetProperty();
	}
}
