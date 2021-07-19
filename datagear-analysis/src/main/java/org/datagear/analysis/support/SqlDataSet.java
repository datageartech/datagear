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
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetProperty.DataType;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.fmk.SqlOutputFormat;
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

	public static final DataSetFmkTemplateResolver SQL_TEMPLATE_RESOLVER = new DataSetFmkTemplateResolver(
			SqlOutputFormat.INSTANCE);

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
	public TemplateResolvedDataSetResult resolve(DataSetQuery query)
			throws DataSetException
	{
		return (TemplateResolvedDataSetResult) super.resolve(query);
	}

	@Override
	protected TemplateResolvedDataSetResult resolveResult(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws DataSetException
	{
		String sql = resolveSqlAsTemplate(getSql(), query);

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
				ResolvedDataSetResult result = resolveResult(cn, rs, query, properties, resolveProperties);

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
	 * @param query
	 * @param properties
	 *            允许为{@code null}
	 * @param resolveProperties
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(Connection cn, ResultSet rs,
			DataSetQuery query, List<DataSetProperty> properties, boolean resolveProperties) throws Throwable
	{
		List<Map<String, ?>> rawData = resolveRawData(cn, rs, query);

		if (resolveProperties)
		{
			List<DataSetProperty> resolvedProperties = resolveProperties(cn, rs, rawData);
			mergeDataSetProperties(resolvedProperties, properties);
			properties = resolvedProperties;
		}

		return resolveResult(rawData, properties, query.getResultDataFormat());
	}

	/**
	 * 解析{@linkplain DataSetProperty}。
	 * 
	 * @param cn
	 * @param rs
	 * @param rawData
	 *            允许为{@code null}
	 * @return
	 * @throws Throwable
	 */
	protected List<DataSetProperty> resolveProperties(Connection cn, ResultSet rs, List<Map<String, ?>> rawData)
			throws Throwable
	{
		JdbcSupport jdbcSupport = getJdbcSupport();
		ResultSetMetaData rsMeta = rs.getMetaData();
		String[] colNames = jdbcSupport.getColumnNames(rsMeta);
		SqlType[] sqlTypes = jdbcSupport.getColumnSqlTypes(rsMeta);

		List<DataSetProperty> properties = new ArrayList<>(colNames.length);

		for (int i = 0; i < colNames.length; i++)
		{
			DataSetProperty property = new DataSetProperty(colNames[i], toPropertyDataType(sqlTypes[i], colNames[i]));

			@JDBCCompatiblity("某些驱动程序可能存在一种情况，列类型会被toPropertyDataType()解析为DataType.UNKNOWN，但是实际值是允许的，"
					+ "比如PostgreSQL-42.2.5驱动对于[SELECT 'aaa' as NAME]语句，结果的SQL类型是Types.OTHER，但实际值是允许的字符串")
			boolean resolveTypeByValue = DataType.UNKNOWN.equals(property.getType());

			if (resolveTypeByValue && rawData != null && rawData.size() > 0)
			{
				Map<String, ?> row0 = rawData.get(0);
				property.setType(resolvePropertyDataType(row0.get(property.getName())));
			}

			properties.add(property);
		}

		return properties;
	}

	/**
	 * 解析原始数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, ?>> resolveRawData(Connection cn, ResultSet rs, DataSetQuery query)
			throws Throwable
	{
		List<Map<String, ?>> data = new ArrayList<>();

		JdbcSupport jdbcSupport = getJdbcSupport();

		ResultSetMetaData rsMeta = rs.getMetaData();
		String[] colNames = jdbcSupport.getColumnNames(rsMeta);
		SqlType[] sqlTypes = jdbcSupport.getColumnSqlTypes(rsMeta);

		checkDataType(cn, rs, colNames, sqlTypes, jdbcSupport);

		while (rs.next())
		{
			if (isReachResultFetchSize(query, data.size()))
				break;

			Map<String, Object> row = new HashMap<>();

			for (int i = 0; i < colNames.length; i++)
			{
				Object value = getColumnValue(cn, rs, colNames[i], sqlTypes[i].getType(), jdbcSupport);
				row.put(colNames[i], value);
			}

			data.add(row);
		}

		return data;
	}

	/**
	 * 校验{@linkplain ResultSet}的数据类型。
	 * 
	 * @param cn
	 * @param rs
	 * @param colNames
	 * @param sqlTypes
	 * @param jdbcSupport
	 * @throws SQLException
	 * @throws SqlDataSetUnsupportedSqlTypeException
	 */
	protected void checkDataType(Connection cn, ResultSet rs, String[] colNames, SqlType[] sqlTypes,
			JdbcSupport jdbcSupport) throws SQLException, SqlDataSetUnsupportedSqlTypeException
	{
		for (int i = 0; i < colNames.length; i++)
			toPropertyDataType(sqlTypes[i], colNames[i]);
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

	/**
	 * 将指定SQL文本作为模板解析。
	 * 
	 * @param sql
	 * @param query
	 * @return
	 */
	protected String resolveSqlAsTemplate(String sql, DataSetQuery query)
	{
		return resolveTextAsTemplate(SQL_TEMPLATE_RESOLVER, sql, query);
	}
}
