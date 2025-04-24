/*
 * Copyright 2018-present datagear.tech
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
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetField.DataType;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.analysis.ResolvedDataSetResult;
import org.datagear.analysis.support.datasettpl.SqlTemplateResult;
import org.datagear.util.IOUtil;
import org.datagear.util.JDBCCompatiblity;
import org.datagear.util.JdbcSupport;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;
import org.datagear.util.SqlParamValue;
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
	private static final long serialVersionUID = 1L;

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

	public SqlDataSet(String id, String name, List<DataSetField> fields, ConnectionFactory connectionFactory,
			String sql)
	{
		super(id, name, fields);
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
	protected TemplateResolvedDataSetResult resolveResult(DataSetQuery query, boolean resolveFields)
			throws DataSetException
	{
		SqlTemplateResult sqlTemplateResult = resolveTemplateResultSql(getSql(), query);

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

			try
			{
				return resolveResult(cn, query, resolveFields, sqlTemplateResult);
			}
			catch (DataSetException e)
			{
				throw e;
			}
			catch (Throwable t)
			{
				throw new DataSetException(t);
			}
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

	protected TemplateResolvedDataSetResult resolveResult(Connection cn, DataSetQuery query, boolean resolveFields,
			SqlTemplateResult sqlTemplateResult) throws Throwable
	{
		String sql = sqlTemplateResult.getResult();
		boolean precompiles = sqlTemplateResult.isPrecompiled();

		// 对于已采用预编译语法的，不进行SQL防注入校验；未采用预编译语法的应该进行SQL防注入校验
		if (!precompiles)
		{
			validateSql(cn, sql);
		}

		Sql sqlObj = Sql.valueOf(sql);
		JdbcSupport jdbcSupport = getJdbcSupport();

		if (precompiles)
		{
			List<SqlParamValue> spvs = jdbcSupport.toSqlParamValues(sqlTemplateResult.getParamValues());
			sqlObj.param(spvs);
		}

		QueryResultSet qrs = executeQuery(cn, sqlObj, jdbcSupport);

		TemplateResolvedDataSetResult dataSetResult = null;

		try
		{
			ResultSet rs = qrs.getResultSet();
			ResolvedDataSetResult result = resolveResult(cn, rs, query, resolveFields);
			dataSetResult = new TemplateResolvedDataSetResult(result.getResult(), result.getFields(), sql);
		}
		finally
		{
			QueryResultSet.close(qrs);
		}

		return dataSetResult;
	}

	protected QueryResultSet executeQuery(Connection cn, Sql sqlObj, JdbcSupport jdbcSupport)
			throws SqlDataSetSqlExecutionException
	{
		QueryResultSet qrs = null;

		try
		{
			qrs = jdbcSupport.executeQuery(cn, sqlObj, ResultSet.TYPE_FORWARD_ONLY);
		}
		catch (Throwable t)
		{
			QueryResultSet.close(qrs);
			throw new SqlDataSetSqlExecutionException(sqlObj.getSqlValue(), t);
		}

		return qrs;
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
	 * @param resolveFields
	 * @return
	 * @throws Throwable
	 */
	protected ResolvedDataSetResult resolveResult(Connection cn, ResultSet rs, DataSetQuery query,
			boolean resolveFields) throws Throwable
	{
		List<DataSetField> rawFields = (resolveFields ? new ArrayList<DataSetField>() : Collections.emptyList());
		List<Map<String, ?>> rawData = resolveRawData(cn, rs, query, resolveFields, rawFields);
		
		if (resolveFields)
			calibrateFields(rawFields, rawData);
		
		return resolveResult(query, rawData, rawFields);
	}

	/**
	 * 解析原始数据。
	 * 
	 * @param cn
	 * @param rs
	 * @param query
	 * @param resolveFields
	 *            是否同时解析{@linkplain DataSetField}并写入下面的{@code fields}中
	 * @param fields
	 * @return
	 * @throws Throwable
	 */
	protected List<Map<String, ?>> resolveRawData(Connection cn, ResultSet rs, DataSetQuery query,
			boolean resolveFields, List<DataSetField> fields) throws Throwable
	{
		List<Map<String, ?>> data = new ArrayList<>();

		JdbcSupport jdbcSupport = getJdbcSupport();

		ResultSetMetaData rsMeta = rs.getMetaData();
		String[] colNames = jdbcSupport.getColumnNames(rsMeta);
		SqlType[] sqlTypes = jdbcSupport.getColumnSqlTypes(rsMeta);
		String[] fieldTypes = new String[colNames.length];
		
		// 无论是否解析fields，都应保留此处逻辑，用于校验数据类型合法
		for (int i = 0; i < colNames.length; i++)
			fieldTypes[i] = toFieldDataType(sqlTypes[i], colNames[i]);
		
		@JDBCCompatiblity("应在遍历ResultSet数据前读取ResultSetMetaData信息解析数据集字段，"
				+ "因为某些驱动在遍历数据后读取ResultSetMetaData会报【ResultSet已关闭】的错误（比如DB2-9.7的db2jcc4.jar驱动）")
		boolean resolveFieldsHere = resolveFields;
		if (resolveFieldsHere)
		{
			for (int i = 0; i < colNames.length; i++)
			{
				DataSetField field = new DataSetField(colNames[i], fieldTypes[i]);
				fields.add(field);
			}
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
		Object value = jdbcSupport.getColumnValueExtract(cn, rs, columnName, sqlType);

		// 对于大字符串类型，value可能是字符输入流，这里应转成字符串并关闭输入流，便于后续处理
		if (value instanceof Reader)
		{
			Reader reader = (Reader) value;
			value = IOUtil.readString(reader, true);
		}

		return value;
	}

	/**
	 * 校准{@linkplain DataSetField}。
	 * <p>
	 * 某些驱动程序可能存在一种情况，列类型会被{@linkplain #toFieldDataType(SqlType, String)}解析为{@linkplain DataType#UNKNOWN}，但是实际值是允许的，
	 * 比如：PostgreSQL-42.2.5驱动对于{@code "SELECT 'aaa' as NAME"}语句，结果的SQL类型是{@linkplain Types#OTHER}，但实际值是允许的字符串。
	 * </p>
	 * <p>
	 * 因此，需要此方法根据实际的数据值重新校准。
	 * </p>
	 * 
	 * @param fields
	 * @param data
	 * @throws Throwable
	 */
	protected void calibrateFields(List<DataSetField> fields, List<Map<String, ?>> data)
			throws Throwable
	{
		if(fields == null || fields.isEmpty())
			return;
		
		if(data == null || data.isEmpty())
			return;
		
		Map<String, ?> row0 = data.get(0);
		
		for (DataSetField field : fields)
		{
			boolean resolveTypeByValue = DataType.UNKNOWN.equals(field.getType());

			if (resolveTypeByValue)
			{
				field.setType(resolveFieldDataType(row0.get(field.getName())));
			}
		}
	}

	/**
	 * 由SQL类型转换为{@linkplain DataSetField#getType()}。
	 * 
	 * @param sqlType
	 * @param columnName
	 *            允许为{@code null}，列名称
	 * @return
	 * @throws SQLException
	 * @throws SqlDataSetUnsupportedSqlTypeException
	 */
	protected String toFieldDataType(SqlType sqlType, String columnName)
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
