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

package org.datagear.dataexchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.meta.resolver.DBMetaResolver;
import org.datagear.persistence.support.PersistenceSupport;
import org.datagear.util.IOUtil;
import org.datagear.util.JdbcUtil;
import org.datagear.util.NumberParserException;
import org.datagear.util.SqlParamValue;
import org.datagear.util.StringUtil;
import org.datagear.util.resource.ResourceFactory;

/**
 * 抽象{@linkplain DevotedDataExchangeService}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataExchangeService<T extends DataExchange> extends PersistenceSupport
		implements DevotedDataExchangeService<T>
{
	public AbstractDevotedDataExchangeService()
	{
		super();
	}

	@Override
	public boolean supports(T dataExchange)
	{
		return true;
	}

	@Override
	public void exchange(T dataExchange) throws DataExchangeException
	{
		onStart(dataExchange);

		DataExchangeContext context = createDataExchangeContext(dataExchange);

		try
		{
			Throwable throwable = null;

			try
			{
				exchange(dataExchange, context);
			}
			catch (Throwable t)
			{
				throwable = t;
			}

			if (throwable == null)
			{
				onSuccess(dataExchange, context);
			}
			else
			{
				DataExchangeException e = wrapToDataExchangeException(throwable);

				onException(dataExchange, context, e);
			}
		}
		finally
		{
			context.closeConnection();
			context.closeContextCloseables();

			onFinish(dataExchange, context);
		}
	}

	/**
	 * 执行数据交换。
	 * 
	 * @param dataExchange
	 * @param context
	 * @throws Throwable
	 */
	protected abstract void exchange(T dataExchange, DataExchangeContext context) throws Throwable;

	/**
	 * 数据交换开始回调。
	 * 
	 * @param dataExchange
	 */
	protected void onStart(T dataExchange)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onStart();
	}

	/**
	 * 数据交换成功回调。
	 * 
	 * @param dataExchange
	 * @param context
	 */
	protected void onSuccess(T dataExchange, DataExchangeContext context)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onSuccess();
	}

	/**
	 * 数据交换异常回调。
	 * 
	 * @param dataExchange
	 * @param context
	 * @param e
	 * @throws DataExchangeException
	 */
	protected void onException(T dataExchange, DataExchangeContext context, DataExchangeException e)
			throws DataExchangeException
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onException(e);
		else
			throw e;
	}

	/**
	 * 数据交换完成。
	 * 
	 * @param dataExchange
	 * @param context
	 */
	protected void onFinish(T dataExchange, DataExchangeContext context)
	{
		DataExchangeListener listener = dataExchange.getListener();

		if (listener != null)
			listener.onFinish();
	}

	/**
	 * 构建{@linkplain DataExchangeContext}。
	 * 
	 * @param dataExchange
	 * @return
	 */
	protected DataExchangeContext createDataExchangeContext(T dataExchange)
	{
		return new DataExchangeContext(dataExchange.getConnectionFactory());
	}

	/**
	 * 处理数据交换异常的事务逻辑。
	 * 
	 * @param context
	 * @param e
	 * @param exceptionResolve
	 * @throws DataExchangeException
	 */
	protected void processTransactionForDataExchangeException(DataExchangeContext context, DataExchangeException e,
			ExceptionResolve exceptionResolve) throws DataExchangeException
	{
		Connection cn = getConnection(context);

		if (ExceptionResolve.ABORT.equals(exceptionResolve) || ExceptionResolve.IGNORE.equals(exceptionResolve))
			JdbcUtil.commitSilently(cn);
		else if (ExceptionResolve.ROLLBACK.equals(exceptionResolve))
			JdbcUtil.rollbackSilently(cn);
		else
			JdbcUtil.rollbackSilently(cn);
	}

	protected Connection getConnection(DataExchangeContext context) throws DataExchangeException
	{
		try
		{
			return context.getConnection();
		}
		catch (Throwable t)
		{
			throw wrapToDataExchangeException(t);
		}
	}

	/**
	 * 提交。
	 * 
	 * @param cn
	 * @throws DataExchangeException
	 */
	protected void commit(Connection cn) throws DataExchangeException
	{
		try
		{
			JdbcUtil.commitIfSupports(cn);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 回滚。
	 * 
	 * @param cn
	 * @throws DataExchangeException
	 */
	protected void rollback(Connection cn) throws DataExchangeException
	{
		try
		{
			JdbcUtil.rollbackIfSupports(cn);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 获取资源。
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @param dataExchangeContext
	 * @return
	 * @throws DataExchangeException
	 */
	protected <R> R getResource(ResourceFactory<R> resourceFactory, DataExchangeContext dataExchangeContext)
			throws DataExchangeException
	{
		try
		{
			R resource = resourceFactory.get();

			dataExchangeContext.addContextCloseable(resourceFactory, resource);

			return resource;
		}
		catch (Exception e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 将异常包装为{@linkplain DataExchangeException}。
	 * 
	 * @param t
	 * @return
	 */
	protected DataExchangeException wrapToDataExchangeException(Throwable t)
	{
		if (t instanceof DataExchangeException)
			return (DataExchangeException) t;
		else
			return new DataExchangeException(t);
	}

	/**
	 * 构建插入预编译SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param columns
	 * @return
	 * @throws DataExchangeException
	 */
	protected String buildInsertPreparedSqlUnchecked(Connection cn, String table, List<Column> columns)
			throws DataExchangeException
	{
		try
		{
			return buildInsertPreparedSql(cn, table, columns);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 构建插入预编译SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param columns
	 * @return
	 * @throws SQLException
	 */
	protected String buildInsertPreparedSql(Connection cn, String table, List<Column> columns) throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		int size = columns.size();

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columns.get(i).getName()).append(quote);
		}

		sql.append(") VALUES (");

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append('?');
		}

		sql.append(")");

		return sql.toString();
	}

	/**
	 * 创建{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatementUnchecked(Connection cn, String sql) throws DataExchangeException
	{
		try
		{
			return cn.prepareStatement(sql);
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 创建{@linkplain PreparedStatement}。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatement(Connection cn, String sql) throws SQLException
	{
		return cn.prepareStatement(sql);
	}

	/**
	 * 扩容集合直到满足长度。
	 * 
	 * @param <G>
	 * @param list
	 * @param size
	 * @param expandValue
	 */
	protected <G> void expandToSize(Collection<G> list, int size, G expandValue)
	{
		int expandCount = (size - list.size());

		for (int i = 0; i < expandCount; i++)
		{
			list.add(expandValue);
		}
	}

	/**
	 * 移除{@linkplain Column}列表中的{@code null}元素。
	 * 
	 * @param columns
	 * @return
	 */
	protected List<Column> removeNullColumns(List<Column> columns)
	{
		List<Column> re = new ArrayList<>(columns.size());

		for (Column column : columns)
		{
			if (column != null)
				re.add(column);
		}

		return re;
	}

	/**
	 * 获取表信息。
	 * 
	 * @param cn
	 * @param table
	 * @param dbMetaResolver
	 * @return
	 * @throws TableNotFoundException
	 */
	protected Table getTableIfValid(Connection cn, String table, DBMetaResolver dbMetaResolver)
			throws TableNotFoundException
	{
		Table t = null;

		try
		{
			t = dbMetaResolver.getTable(cn, table);
		}
		catch (org.datagear.meta.resolver.TableNotFoundException e)
		{
			String exactName = dbMetaResolver.getExactTableName(cn, table);

			if (exactName == null || exactName.equals(table))
				throw new TableNotFoundException(table);
			else
			{
				try
				{
					t = dbMetaResolver.getTable(cn, exactName);
				}
				catch (org.datagear.meta.resolver.TableNotFoundException e1)
				{
					throw new TableNotFoundException(table);
				}
			}
		}

		if (t == null || !t.hasColumn())
			throw new TableNotFoundException(table);

		return t;
	}

	/**
	 * 查找表列。
	 * 
	 * @param table
	 * @param columnNames
	 * @param nullIfColumnNotFound
	 *            当为{@code false}时，如果未找到指定名称的列，将抛出{@linkplain ColumnNotFoundException}异常
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected List<Column> findColumns(Table table, List<String> columnNames, boolean nullIfColumnNotFound)
			throws ColumnNotFoundException
	{
		int size = columnNames.size();

		List<Column> columns = new ArrayList<>(size);

		for (String columnName : columnNames)
		{
			Column column = table.getColumn(columnName);

			if (!nullIfColumnNotFound && column == null)
				throw new ColumnNotFoundException(table.getName(), columnName);

			columns.add(column);
		}

		return columns;
	}

	/**
	 * 是否外键列。
	 * 
	 * @param table
	 * @param columns
	 * @return
	 */
	protected List<Boolean> isImportKeyColumns(Table table, Column[] columns)
	{
		return isImportKeyColumns(table, Arrays.asList(columns));
	}

	/**
	 * 是否外键列。
	 * 
	 * @param table
	 * @param columns
	 * @return
	 */
	protected List<Boolean> isImportKeyColumns(Table table, List<Column> columns)
	{
		List<Boolean> re = new ArrayList<Boolean>(columns.size());

		for (Column column : columns)
		{
			re.add(table.isImportKeyColumn(column.getName()));
		}

		return re;
	}

	/**
	 * 移除{@code null}列信息位置对应的值。
	 * 
	 * @param columns
	 * @param columnValues
	 * @return
	 */
	protected <G> List<G> removeValueOfNullColumn(List<Column> columns, List<G> columnValues)
	{
		List<G> re = new ArrayList<>(columnValues.size());

		for (int i = 0, len = columns.size(); i < len; i++)
		{
			if (columns.get(i) != null)
			{
				re.add(columnValues.get(i));
			}
		}

		return re;
	}

	/**
	 * 移除{@code null}列信息位置对应的值。
	 * 
	 * @param <G>
	 * @param columns
	 * @param columnValues
	 * @param expandValue
	 * @return
	 */
	protected <G> List<G> removeValueOfNullColumnExpanded(List<Column> columns, List<G> columnValues, G expandValue)
	{
		expandToSize(columnValues, columns.size(), expandValue);
		return removeValueOfNullColumn(columns, columnValues);
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @param dbMetaResolver
	 * @return
	 * @throws SQLException
	 */
	protected List<Column> getColumns(Connection cn, ResultSet rs, DBMetaResolver dbMetaResolver) throws SQLException
	{
		ResultSetMetaData resultSetMetaData = rs.getMetaData();
		Column[] columns = dbMetaResolver.getColumns(cn, resultSetMetaData);

		List<Column> list = new ArrayList<>(columns.length);

		for (Column column : columns)
			list.add(column);

		return list;
	}

	/**
	 * 查找指定名称的列信息。
	 * <p>
	 * 没找到将返回{@code null}。
	 * </p>
	 * 
	 * @param columns
	 * @param name
	 * @return
	 */
	protected Column findColumn(Collection<? extends Column> columns, String name)
	{
		for (Column column : columns)
		{
			if (column.getName().equals(name))
				return column;
		}

		return null;
	}

	/**
	 * 将空字符串的外键列值设为{@code null}。
	 * 
	 * @param <V>
	 * @param columns
	 * @param importKeyColumns
	 * @param columnValues
	 */
	protected <V> void setNullForEmptyIfImportKey(List<Column> columns, List<Boolean> importKeyColumns,
			List<V> columnValues)
	{
		for (int i = 0, len = columns.size(); i < len; i++)
		{
			Column col = columns.get(i);

			// 只有在列允许null值时才应设置
			if (col.isNullable())
			{
				boolean ikc = importKeyColumns.get(i);

				if (ikc)
				{
					V cv = columnValues.get(i);

					if ((cv instanceof String) && StringUtil.isEmpty((String) cv))
					{
						columnValues.set(i, null);
					}
				}
			}
		}
	}

	/**
	 * 设置{@linkplain PreparedStatement}的参数值，并在必要时进行数据类型转换。
	 * 
	 * @param cn
	 * @param st
	 * @param paramIndex
	 * @param paramValue
	 * @param column
	 * @param dataFormatContext
	 *            允许为{@code null}，当{@code parameterValue}为字符串且需要类型转换时使用
	 * @throws SQLException
	 * @throws ParseException
	 * @throws NumberParserException
	 * @throws DecoderException
	 * @throws UnsupportedSqlValueException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setParamValue(Connection cn, PreparedStatement st, int paramIndex, Object paramValue,
			Column column, DataFormatContext dataFormatContext) throws SQLException, ParseException,
			NumberParserException, DecoderException, UnsupportedSqlValueException, UnsupportedSqlTypeException
	{
		int sqlType = column.getType();

		if (paramValue == null)
		{
			st.setNull(paramIndex, sqlType);
			return;
		}

		Object value = paramValue;

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			{
				if (!(value instanceof String))
					value = value.toString();

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseBigDecimal((String) paramValue);

				break;
			}

			case Types.TINYINT:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseByteIfExact((String) paramValue);

				break;
			}

			case Types.SMALLINT:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseShortIfExact((String) paramValue);

				break;
			}

			case Types.INTEGER:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseIntIfExact((String) paramValue);

				break;
			}

			case Types.BIGINT:
			{
				if (paramValue instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseLongIfExact((String) paramValue);

				break;
			}

			case Types.REAL:
			case Types.FLOAT:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseFloatIfExact((String) paramValue);

				break;
			}

			case Types.DOUBLE:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseDoubleIfExact((String) paramValue);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseBytes((String) paramValue);

				break;
			}

			case Types.DATE:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseDate((String) paramValue);

				break;
			}

			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseTime((String) paramValue);

				break;
			}

			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseTimestamp((String) paramValue);

				break;
			}

			case Types.BLOB:
			{
				if (value instanceof String && dataFormatContext != null)
					value = dataFormatContext.parseBytes((String) paramValue);

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				if (!(value instanceof String))
					value = paramValue.toString();

				break;
			}

			case Types.NCLOB:
			{
				if (!(value instanceof String))
					value = paramValue.toString();

				break;
			}

			case Types.SQLXML:
			{
				if (!(value instanceof String))
					value = paramValue.toString();

				break;
			}

			default:
			{
			}
		}

		super.setParamValue(cn, st, paramIndex, SqlParamValue.valueOf(value, sqlType));
	}

	@Override
	protected Object setParamValueExt(Connection cn, PreparedStatement st, int paramIndex, SqlParamValue paramValue)
			throws SQLException
	{
		throw new UnsupportedSqlValueException(paramValue.getType(), paramValue.getValue());
	}

	/**
	 * 获取简单字段值。
	 * <p>
	 * 此方法会对部分JDBC数据进行转换，仅返回如下类型的对象：
	 * </p>
	 * <p>
	 * {@linkplain String}、{@linkplain Number}子类、{@linkplain Boolean}、{@linkplain java.sql.Date}、
	 * {@linkplain java.sql.Timestamp}、{@linkplain java.sql.Time}、{@code byte[]}。
	 * </p>
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected Object getColumnValueSimple(Connection cn, ResultSet rs, Column column)
			throws SQLException, IOException, UnsupportedSqlTypeException
	{
		Object value = null;

		int sqlType = column.getType();
		String columnName = column.getName();

		switch (sqlType)
		{
			case Types.LONGVARCHAR:
			{
				Reader reader = rs.getCharacterStream(columnName);

				try
				{
					if (!rs.wasNull())
						value = readToString(reader);
				}
				finally
				{
					IOUtil.close(reader);
				}

				break;
			}

			case Types.LONGVARBINARY:
			{
				InputStream in = rs.getBinaryStream(columnName);

				try
				{
					if (!rs.wasNull())
					{
						value = readToBytes(in);
					}
				}
				finally
				{
					IOUtil.close(in);
				}

				break;
			}

			case Types.CLOB:
			{
				Clob clob = rs.getClob(columnName);

				if (!rs.wasNull())
				{
					Reader reader = clob.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			case Types.BLOB:
			{
				Blob blob = rs.getBlob(columnName);

				if (!rs.wasNull())
				{
					InputStream inputStream = blob.getBinaryStream();

					try
					{
						value = readToBytes(inputStream);
					}
					finally
					{
						IOUtil.close(inputStream);
					}
				}

				break;
			}

			case Types.LONGNVARCHAR:
			{
				Reader reader = rs.getNCharacterStream(columnName);

				try
				{
					if (!rs.wasNull())
						value = readToString(reader);
				}
				finally
				{
					IOUtil.close(reader);
				}

				break;
			}

			case Types.NCLOB:
			{
				NClob nclob = rs.getNClob(columnName);

				if (!rs.wasNull())
				{
					Reader reader = nclob.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			case Types.ROWID:
			{
				RowId rowId = rs.getRowId(columnName);

				if (!rs.wasNull())
					value = rowId.getBytes();

				break;
			}

			case Types.SQLXML:
			{
				SQLXML sqlXml = rs.getSQLXML(columnName);

				if (!rs.wasNull())
				{
					Reader reader = sqlXml.getCharacterStream();

					try
					{
						value = readToString(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				break;
			}

			case Types.ARRAY:
			case Types.DATALINK:
			case Types.DISTINCT:
			case Types.JAVA_OBJECT:
			case Types.OTHER:
			case Types.REF:
			case Types.REF_CURSOR:
			{
				value = getColumnValueExt(cn, rs, columnName, sqlType);

				break;
			}

			default:

				value = super.getColumnValue(cn, rs, columnName, sqlType);
		}

		if (rs.wasNull())
			value = null;

		return value;
	}

	@Override
	protected Object getColumnValueExt(Connection cn, ResultSet rs, String columnName, int sqlType) throws SQLException
	{
		throw new UnsupportedSqlTypeException(sqlType);
	}

	/**
	 * 获取字段的的字符串值。
	 * 
	 * @param cn
	 * @param rs
	 * @param column
	 * @param dataFormatContext
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UnsupportedSqlTypeException
	 */
	protected String getStringValue(Connection cn, ResultSet rs, Column column,
			DataFormatContext dataFormatContext) throws SQLException, IOException, UnsupportedSqlTypeException
	{
		Object value = getColumnValueSimple(cn, rs, column);
		String valueStr = null;

		if (value == null)
			;
		else if (value instanceof Number)
		{
			valueStr = dataFormatContext.formatNumber((Number) value);
		}
		else if (value instanceof java.util.Date)
		{
			if (value instanceof java.sql.Date)
				valueStr = dataFormatContext.formatDate((java.sql.Date) value);
			else if (value instanceof java.sql.Time)
				valueStr = dataFormatContext.formatTime((Time) value);
			else if (value instanceof java.sql.Timestamp)
				valueStr = dataFormatContext.formatTimestamp((Timestamp) value);
			else
				valueStr = dataFormatContext.formatDate(new java.sql.Date(((java.util.Date) value).getTime()));
		}
		else if (value instanceof String)
		{
			valueStr = (String) value;
		}
		else if (value instanceof Boolean)
		{
			if (Types.BIT == column.getType())
				valueStr = Boolean.TRUE.equals(value) ? "1" : "0";
			else
				valueStr = value.toString();
		}
		else if (value instanceof byte[])
		{
			valueStr = dataFormatContext.formatBytes((byte[]) value);
		}
		else
			valueStr = value.toString();

		return valueStr;
	}

	/**
	 * 将字节流读入字节数组。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected byte[] readToBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		byte[] buf = new byte[32];
		int count = -1;
		while ((count = in.read(buf)) > -1)
			bout.write(buf, 0, count);

		return bout.toByteArray();
	}

	/**
	 * 将字符流读入字符串。
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	protected String readToString(Reader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();

		char[] buf = new char[32];

		int count = -1;
		while ((count = reader.read(buf)) > -1)
			sb.append(buf, 0, count);

		return sb.toString();
	}

	/**
	 * 导入一条值数据。
	 * 
	 * @param cn
	 * @param st
	 * @param columns
	 * @param columnValues
	 * @param dataIndex
	 * @param nullForIllegalColumnValue
	 * @param exceptionResolve
	 * @param dataFormatContext
	 * @param listener
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean importValueData(Connection cn, PreparedStatement st, List<Column> columns,
			List<? extends Object> columnValues, DataIndex dataIndex, boolean nullForIllegalColumnValue,
			ExceptionResolve exceptionResolve, DataFormatContext dataFormatContext,
			ValueDataImportListener listener)
			throws DataExchangeException
	{
		DataExchangeException exception = null;

		try
		{
			setImportParamValues(cn, st, columns, columnValues, dataIndex, nullForIllegalColumnValue,
					dataFormatContext, listener);

			executeImportPreparedStatement(st, dataIndex);
		}
		catch (Throwable t)
		{
			exception = wrapToDataExchangeException(t);
		}

		if (exception == null)
		{
			if (listener != null)
				listener.onSuccess(dataIndex);

			return true;
		}
		else
		{
			if (ExceptionResolve.IGNORE.equals(exceptionResolve))
			{
				if (listener != null)
					listener.onIgnore(dataIndex, exception);

				return false;
			}
			else
				throw exception;
		}
	}

	/**
	 * 执行导入SQL。
	 * 
	 * @param impt
	 * @param st
	 * @param context
	 * @throws ExecuteDataImportSqlException
	 */
	protected void executeImportPreparedStatement(PreparedStatement st, DataIndex dataIndex)
			throws ExecuteDataImportSqlException
	{
		try
		{
			st.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new ExecuteDataImportSqlException(dataIndex, e);
		}
	}

	/**
	 * 设置导入{@linkplain PreparedStatement}参数值。
	 * 
	 * @param cn
	 * @param st
	 * @param columns
	 * @param columnValues
	 * @param dataIndex
	 * @param nullForIllegalColumnValue
	 * @param dataFormatContext
	 * @param listener
	 * @throws SetImportColumnValueException
	 */
	protected void setImportParamValues(Connection cn, PreparedStatement st, List<Column> columns,
			List<? extends Object> columnValues, DataIndex dataIndex, boolean nullForIllegalColumnValue,
			DataFormatContext dataFormatContext, ValueDataImportListener listener) throws SetImportColumnValueException
	{
		int columnCount = columns.size();
		int columnValueCount = columnValues.size();

		for (int i = 0; i < columnCount; i++)
		{
			Column column = columns.get(i);
			String columnName = column.getName();
			int parameterIndex = i + 1;
			Object rawValue = (columnValues == null || columnValueCount - 1 < i ? null : columnValues.get(i));

			try
			{
				setParamValue(cn, st, parameterIndex, rawValue, column, dataFormatContext);
			}
			catch (Throwable t)
			{
				SetImportColumnValueException e = null;

				if ((t instanceof ParseException) || (t instanceof NumberParserException)
						|| (t instanceof DecoderException) || (t instanceof UnsupportedSqlValueException))
					e = new IllegalImportSourceValueException(dataIndex, columnName, rawValue, t);
				else
					e = new SetImportColumnValueException(dataIndex, columnName, rawValue, t);

				if (nullForIllegalColumnValue)
				{
					try
					{
						st.setNull(parameterIndex, column.getType());
					}
					catch (SQLException e1)
					{
						throw new SetImportColumnValueException(dataIndex, columnName, null);
					}

					if (listener != null)
					{
						listener.onSetNullColumnValue(dataIndex, columnName, rawValue, e);
					}
				}
				else
					throw e;
			}
		}
	}
}
