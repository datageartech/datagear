/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.analysis.support;

import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
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
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcSupport;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;
import org.datagear.util.SqlType;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.util.sqlvalidator.SqlValidator;
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

	private SqlValidator sqlValidator = null;

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

	public SqlValidator getSqlValidator()
	{
		return sqlValidator;
	}

	public void setSqlValidator(SqlValidator sqlValidator)
	{
		this.sqlValidator = sqlValidator;
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
		String sql = resolveTemplateSql(getSql(), query);

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

			validateSql(cn, sql);

			Sql sqlObj = Sql.valueOf(sql);

			JdbcSupport jdbcSupport = getJdbcSupport();

			QueryResultSet qrs = null;

			try
			{
				qrs = jdbcSupport.executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
			}
			catch (Throwable t)
			{
				QueryResultSet.close(qrs);
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
			finally
			{
				QueryResultSet.close(qrs);
			}

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
	 * 校验SQL。
	 * 
	 * @param cn
	 * @param sql
	 * @throws SqlDataSetSqlValidationException
	 */
	protected void validateSql(Connection cn, String sql) throws SqlDataSetSqlValidationException
	{
		if (this.sqlValidator == null)
			return;

		SqlValidation validation = this.sqlValidator.validate(sql, DatabaseProfile.valueOf(cn));

		if (!validation.isValid())
			throw new SqlDataSetSqlValidationException(sql, validation);
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
		List<DataSetProperty> rawProperties =(resolveProperties ? new ArrayList<DataSetProperty>() : Collections.emptyList());
		List<Map<String, ?>> rawData = resolveRawData(cn, rs, query, resolveProperties, rawProperties);
		
		if(resolveProperties)
			calibrateProperties(rawProperties, rawData);
		
		return resolveResult(query, rawData, rawProperties, properties, resolveProperties);
	}

	/**
	 * 解析原始数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param query
	 * @param resolveProperties 是否同时解析{@linkplain DataSetProperty}并写入下面的{@code properties}中
	 * @param properties
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, ?>> resolveRawData(Connection cn, ResultSet rs, DataSetQuery query,
			boolean resolveProperties, List<DataSetProperty> properties) throws Throwable
	{
		List<Map<String, ?>> data = new ArrayList<>();

		JdbcSupport jdbcSupport = getJdbcSupport();

		ResultSetMetaData rsMeta = rs.getMetaData();
		String[] colNames = jdbcSupport.getColumnNames(rsMeta);
		SqlType[] sqlTypes = jdbcSupport.getColumnSqlTypes(rsMeta);
		String[] propertyTypes = new String[colNames.length];
		
		//无论是否解析properties，都应保留此处逻辑，用于校验数据类型合法
		for (int i = 0; i < colNames.length; i++)
			propertyTypes[i] = toPropertyDataType(sqlTypes[i], colNames[i]);
		
		if(resolveProperties)
		{
			@JDBCCompatiblity("应在遍历ResultSet数据前读取ResultSetMetaData信息解析数据集属性，"
								+"因为某些驱动在遍历数据后读取ResultSetMetaData会报【ResultSet已关闭】的错误（比如DB2-9.7驱动）")
			List<DataSetProperty> localProperties = new ArrayList<>(colNames.length);
			for (int i = 0; i < colNames.length; i++)
			{
				DataSetProperty property = new DataSetProperty(colNames[i], propertyTypes[i]);
				localProperties.add(property);
			}
			
			properties.addAll(localProperties);
		}
		
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
	 * 校准{@linkplain DataSetProperty}。
	 * <p>
	 * 某些驱动程序可能存在一种情况，列类型会被{@linkplain #toPropertyDataType(SqlType, String)}解析为{@linkplain DataType#UNKNOWN}，但是实际值是允许的，
	 * 比如：PostgreSQL-42.2.5驱动对于{@code "SELECT 'aaa' as NAME"}语句，结果的SQL类型是{@linkplain Types#OTHER}，但实际值是允许的字符串。
	 * </p>
	 * <p>
	 * 因此，需要此方法根据实际的数据值重新校准。
	 * </p>
	 * 
	 * @param properties
	 * @param data
	 * @throws Throwable
	 */
	protected void calibrateProperties(List<DataSetProperty> properties, List<Map<String, ?>> data)
			throws Throwable
	{
		if(properties == null || properties.isEmpty())
			return;
		
		if(data == null || data.isEmpty())
			return;
		
		Map<String, ?> row0 = data.get(0);
		
		for (DataSetProperty property : properties)
		{
			boolean resolveTypeByValue = DataType.UNKNOWN.equals(property.getType());

			if (resolveTypeByValue)
			{
				property.setType(resolvePropertyDataType(row0.get(property.getName())));
			}
		}
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
