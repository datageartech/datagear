/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetOption;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetProperty.DataType;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcSupport;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;
import org.datagear.util.SqlType;
import org.datagear.util.resource.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL {@linkplain DataSet}。
 * <p>
 * 此类的{@linkplain #getSql()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSet extends AbstractResolvableDataSet implements ResolvableDataSet
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SqlDataSet.class);

	protected static final JdbcSupport JDBC_SUPPORT = new JdbcSupport();

	private ConnectionFactory connectionFactory;

	private String sql;

	public SqlDataSet()
	{
		super();
	}

	public SqlDataSet(String id, String name, ConnectionFactory connectionFactory, String sql)
	{
		super(id, name);
		this.connectionFactory = connectionFactory;
		this.sql = sql;
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
	public TemplateResolvedDataSetResult resolve(Map<String, ?> paramValues, DataSetOption dataSetOption)
			throws DataSetException
	{
		return resolveResult(paramValues, null, dataSetOption);
	}

	@Override
	protected TemplateResolvedDataSetResult resolveResult(Map<String, ?> paramValues, List<DataSetProperty> properties,
			DataSetOption dataSetOption) throws DataSetException
	{
		String sql = resolveAsFmkTemplate(getSql(), paramValues);

		Connection cn = null;

		try
		{
			try
			{
				cn = getConnectionFactory().get();
			}
			catch (Throwable t)
			{
				throw new SqlDataSetConnectionException(t);
			}

			Sql sqlObj = Sql.valueOf(sql);

			JdbcSupport jdbcSupport = getJdbcSupport();

			QueryResultSet qrs = null;

			try
			{
				qrs = jdbcSupport.executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
			}
			catch (Throwable t)
			{
				throw new SqlDataSetSqlExecutionException(sql, t);
			}

			TemplateResolvedDataSetResult dataSetResult = null;

			try
			{
				ResultSet rs = qrs.getResultSet();
				ResolvedDataSetResult result = resolveResult(cn, rs, properties, dataSetOption);

				dataSetResult = new TemplateResolvedDataSetResult(result.getResult(), result.getProperties(), sql);
			}
			catch (DataSetException e)
			{
				throw e;
			}
			catch (Throwable t)
			{
				throw new DataSetException(t);
			}

			QueryResultSet.close(qrs);

			return dataSetResult;
		}
		finally
		{
			if (cn != null)
			{
				try
				{
					getConnectionFactory().release(cn);
				}
				catch (Throwable t)
				{
					LOGGER.error("Release connection error", t);
				}
			}
		}
	}

	/**
	 * 解析结果。
	 * 
	 * @param cn
	 * @param rs
	 * @param properties
	 *            允许为{@code null}，此时会自动解析
	 * @param dataSetOption
	 *            允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(Connection cn, ResultSet rs, List<DataSetProperty> properties,
			DataSetOption dataSetOption) throws Throwable
	{
		boolean resolveProperties = (properties == null || properties.isEmpty());

		List<Map<String, ?>> data = new ArrayList<>();

		JdbcSupport jdbcSupport = getJdbcSupport();
		DataSetPropertyValueConverter converter = createDataSetPropertyValueConverter();

		ResultSetMetaData rsMeta = rs.getMetaData();
		String[] colNames = jdbcSupport.getColumnNames(rsMeta);
		SqlType[] sqlTypes = jdbcSupport.getColumnSqlTypes(rsMeta);

		if (resolveProperties)
		{
			properties = new ArrayList<>(colNames.length);
			for (int i = 0; i < colNames.length; i++)
				properties.add(new DataSetProperty(colNames[i], toPropertyDataType(sqlTypes[i], colNames[i])));
		}

		int maxColumnSize = Math.min(colNames.length, properties.size());

		int rowIdx = 0;

		while (rs.next())
		{
			Map<String, Object> row = new HashMap<>();

			for (int i = 0; i < maxColumnSize; i++)
			{
				DataSetProperty property = properties.get(i);

				Object value = getColumnValue(cn, rs, colNames[i], sqlTypes[i].getType(), jdbcSupport);

				if (resolveProperties && rowIdx == 0)
				{
					@JDBCCompatiblity("某些驱动程序可能存在一种情况，列类型会被toPropertyDataType()解析为DataType.UNKNOWN，但是实际值是允许的，"
							+ "比如PostgreSQL-42.2.5驱动对于[SELECT 'aaa' as NAME]语句，结果的SQL类型是Types.OTHER，但实际值是允许的字符串")
					boolean resolveTypeByValue = (DataType.UNKNOWN.equals(property.getType()));

					if (resolveTypeByValue)
						property.setType(resolvePropertyDataType(value));
				}

				value = convertToPropertyDataType(converter, value, property);

				row.put(property.getName(), value);
			}

			boolean reachMaxCount = isReachResultDataMaxCount(dataSetOption, data.size());
			boolean breakLoop = reachMaxCount;

			if (!reachMaxCount)
				data.add(row);

			rowIdx++;

			if (breakLoop)
				break;
		}

		DataSetResult result = new DataSetResult(data);

		return new ResolvedDataSetResult(result, properties);
	}

	protected Object getColumnValue(Connection cn, ResultSet rs, String columnName, int sqlType,
			JdbcSupport jdbcSupport) throws Throwable
	{
		Object value = jdbcSupport.getColumnValue(cn, rs, columnName, sqlType);

		// 对于大字符串类型，value可能是字符输入流，这里应转成字符串并关闭输入流，便于后续处理
		if (value instanceof Reader)
		{
			Reader reader = (Reader) value;
			value = IOUtil.readString(reader, true);
		}

		return value;
	}

	/**
	 * 由SQL类型转换为{@linkplain DataSetProperty#getType()}。
	 * 
	 * @param sqlType
	 * @param columnName
	 *            允许为{@code null}，列名称
	 * @return
	 * @throws SQLException
	 * @throws SqlDataSetUnsupportedSqlTypeException
	 */
	protected String toPropertyDataType(SqlType sqlType, String columnName)
			throws SQLException, SqlDataSetUnsupportedSqlTypeException
	{
		String dataType = null;

		int type = sqlType.getType();

		switch (type)
		{
			// 确定不支持的类型
			case Types.BINARY:
			case Types.BLOB:
			case Types.LONGVARBINARY:
			case Types.VARBINARY:
				throw new SqlDataSetUnsupportedSqlTypeException(sqlType, columnName);

			case Types.CHAR:
			case Types.NCHAR:
			case Types.VARCHAR:
			case Types.NVARCHAR:
			case Types.LONGVARCHAR:
			case Types.LONGNVARCHAR:
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
			case Types.TIME_WITH_TIMEZONE:
			{
				dataType = DataType.TIME;
				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				dataType = DataType.TIMESTAMP;
				break;
			}

			default:
				dataType = DataType.UNKNOWN;
		}

		return dataType;
	}

	protected JdbcSupport getJdbcSupport()
	{
		return JDBC_SUPPORT;
	}
}
