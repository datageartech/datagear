/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TextDataImport;
import org.datagear.dataexchange.TextDataImportListener;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本导入服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedTextDataImportService<T extends TextDataImport>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedTextDataImportService()
	{
		super();
	}

	public AbstractDevotedTextDataImportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	/**
	 * 构建{@linkplain TextDataImportContext}。
	 * 
	 * @param impt
	 * @return
	 */
	protected TextDataImportContext buildTextDataImportContext(T impt)
	{
		return new TextDataImportContext(new DataFormatContext(impt.getDataFormat()));
	}

	/**
	 * 导入下一条数据。
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param textDataImportContext
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean importNextData(T impt, Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<String> columnValues, TextDataImportContext textDataImportContext) throws DataExchangeException
	{
		TextDataImportListener listener = impt.getListener();

		try
		{
			setImportColumnValues(impt, cn, st, columnInfos, columnValues, textDataImportContext);
			executeNextImport(impt, st, textDataImportContext);

			if (listener != null)
				listener.onSuccess(textDataImportContext.getDataIndex());

			return true;
		}
		catch (Throwable t)
		{
			DataExchangeException de = wrapToDataExchangeException(t);

			if (ExceptionResolve.IGNORE.equals(impt.getImportOption().getExceptionResolve()))
			{
				if (listener != null)
					listener.onIgnore(textDataImportContext.getDataIndex(), de);

				return false;
			}
			else
				throw de;
		}
		finally
		{
			textDataImportContext.incrementDataIndex();
			textDataImportContext.clearCloseResources();
		}
	}

	/**
	 * 执行下一个导入操作。
	 * 
	 * @param impt
	 * @param st
	 * @param textDataImportContext
	 * @throws ExecuteDataImportSqlException
	 */
	protected void executeNextImport(T impt, PreparedStatement st, TextDataImportContext textDataImportContext)
			throws ExecuteDataImportSqlException
	{
		try
		{
			st.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new ExecuteDataImportSqlException(textDataImportContext.getDataIndex(), e);
		}
	}

	/**
	 * 设置文本导入数据参数，并进行必要的数据类型转换。
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param textDataImportContext
	 * @throws SetImportColumnValueException
	 */
	protected void setImportColumnValues(T impt, Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<String> columnValues, TextDataImportContext textDataImportContext) throws SetImportColumnValueException
	{
		int dataIndex = textDataImportContext.getDataIndex();

		int columnCount = columnInfos.size();
		int columnValueCount = columnValues.size();

		for (int i = 0; i < columnCount; i++)
		{
			ColumnInfo columnInfo = columnInfos.get(i);
			String columnName = columnInfo.getName();
			int sqlType = columnInfo.getType();
			int parameterIndex = i + 1;
			String rawValue = (columnValues == null || columnValueCount - 1 < i ? null : columnValues.get(i));

			try
			{
				setImportColumnValue(impt, cn, st, parameterIndex, sqlType, rawValue, textDataImportContext);
			}
			catch (Exception e)
			{
				if (impt.getImportOption().isNullForIllegalColumnValue())
				{
					try
					{
						st.setNull(parameterIndex, sqlType);
					}
					catch (SQLException e1)
					{
						throw new SetImportColumnValueException(dataIndex, columnName, null);
					}

					TextDataImportListener listener = impt.getListener();
					if (listener != null)
					{
						DataExchangeException de = null;

						if ((e instanceof ParseException) || (e instanceof DecoderException))
							de = new IllegalSourceValueException(dataIndex, columnName, rawValue, e);
						else if (e instanceof UnsupportedSqlTypeException)
							de = (UnsupportedSqlTypeException) e;
						else
							de = new SetImportColumnValueException(dataIndex, columnName, rawValue);

						listener.onSetNullColumnValue(dataIndex, columnName, rawValue, de);
					}
				}
				else
				{
					if ((e instanceof ParseException) || (e instanceof DecoderException))
					{
						throw new IllegalSourceValueException(dataIndex, columnName, rawValue, e);
					}
					else if (e instanceof UnsupportedSqlTypeException)
					{
						throw (UnsupportedSqlTypeException) e;
					}
					else
					{
						throw new SetImportColumnValueException(dataIndex, columnName, rawValue);
					}
				}
			}
		}
	}

	/**
	 * 设置文本导入数据参数，并进行必要的数据类型转换。
	 * <p>
	 * 此方法实现参考自JDBC4.0规范“Data Type Conversion Tables”章节中的“Java Types Mapper to
	 * JDBC Types”表。
	 * </p>
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param parameterIndex
	 * @param sqlType
	 * @param parameterValue
	 * @param textDataImportContext
	 * @throws SQLException
	 * @throws ParseException
	 * @throws DecoderException
	 * @throws UnsupportedSqlTypeException
	 */
	protected void setImportColumnValue(T impt, Connection cn, PreparedStatement st, int parameterIndex, int sqlType,
			String parameterValue, TextDataImportContext textDataImportContext)
			throws SQLException, ParseException, DecoderException, UnsupportedSqlTypeException
	{
		if (parameterValue == null)
		{
			st.setNull(parameterIndex, sqlType);
			return;
		}

		DataFormatContext dataFormatContext = textDataImportContext.getDataFormatContext();

		switch (sqlType)
		{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			{
				st.setString(parameterIndex, parameterValue);

				break;
			}

			case Types.NUMERIC:
			case Types.DECIMAL:
			{
				BigDecimal value = new BigDecimal(parameterValue);
				st.setBigDecimal(parameterIndex, value);

				break;
			}

			case Types.BIT:
			case Types.BOOLEAN:
			{
				boolean value = ("true".equalsIgnoreCase(parameterValue) || "1".equals(parameterValue)
						|| "on".equalsIgnoreCase(parameterValue));
				st.setBoolean(parameterIndex, value);

				break;
			}

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			{
				Integer value = dataFormatContext.parseInt(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setInt(parameterIndex, value);

				break;
			}

			case Types.BIGINT:
			{
				Long value = dataFormatContext.parseLong(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setLong(parameterIndex, value);

				break;
			}

			case Types.REAL:
			{
				Float value = dataFormatContext.parseFloat(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setFloat(parameterIndex, value);

				break;
			}

			case Types.FLOAT:
			case Types.DOUBLE:
			{
				Double value = dataFormatContext.parseDouble(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setDouble(parameterIndex, value);

				break;
			}

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			{
				byte[] value = dataFormatContext.parseBytes(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setBytes(parameterIndex, value);

				break;
			}

			case Types.DATE:
			{
				java.sql.Date value = dataFormatContext.parseDate(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setDate(parameterIndex, value);

				break;
			}

			case Types.TIME:
			{
				java.sql.Time value = dataFormatContext.parseTime(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTime(parameterIndex, value);

				break;
			}

			case Types.TIMESTAMP:
			{
				java.sql.Timestamp value = dataFormatContext.parseTimestamp(parameterValue);

				if (value == null)
					st.setNull(parameterIndex, sqlType);
				else
					st.setTimestamp(parameterIndex, value);

				break;
			}

			case Types.CLOB:
			{
				Clob clob = cn.createClob();
				clob.setString(1, parameterValue);
				st.setClob(parameterIndex, clob);

				break;
			}

			case Types.BLOB:
			{
				byte[] value = dataFormatContext.parseBytes(parameterValue);

				if (value == null)
				{
					st.setNull(parameterIndex, sqlType);
				}
				else
				{
					Blob blob = cn.createBlob();
					blob.setBytes(1, value);
					st.setBlob(parameterIndex, blob);
				}

				break;
			}

			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.LONGNVARCHAR:
			{
				st.setNString(parameterIndex, parameterValue);
				break;
			}

			case Types.NCLOB:
			{
				NClob nclob = cn.createNClob();
				nclob.setString(1, parameterValue);
				st.setNClob(parameterIndex, nclob);
				break;
			}

			case Types.SQLXML:
			{
				SQLXML sqlxml = cn.createSQLXML();
				sqlxml.setString(parameterValue);
				st.setSQLXML(parameterIndex, sqlxml);
				break;
			}

			default:

				throw new UnsupportedSqlTypeException(sqlType);
		}
	}

	/**
	 * 获取表指定列信息列表。
	 * <p>
	 * 当指定位置的列不存在时，如果{@code nullIfColumnNotFound}为{@code true}，返回列表对应位置将为{@code null}，
	 * 否则，将立刻抛出{@linkplain ColumnNotFoundException}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @param nullIfColumnNotFound
	 * @return
	 * @throws TableNotFoundException
	 * @throws ColumnNotFoundException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, String table, List<String> columnNames,
			boolean nullIfColumnNotFound) throws TableNotFoundException, ColumnNotFoundException
	{
		return getColumnInfos(cn, table, columnNames, nullIfColumnNotFound, this.databaseInfoResolver);
	}

	/**
	 * 文本数据导入上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextDataImportContext extends DataExchangeContext
	{
		private DataFormatContext dataFormatContext;

		/** 导入数据索引 */
		private int dataIndex = 0;

		public TextDataImportContext()
		{
			super();
		}

		public TextDataImportContext(DataFormatContext dataFormatContext)
		{
			super();
			this.dataFormatContext = dataFormatContext;
		}

		public DataFormatContext getDataFormatContext()
		{
			return dataFormatContext;
		}

		public void setDataFormatContext(DataFormatContext dataFormatContext)
		{
			this.dataFormatContext = dataFormatContext;
		}

		public int getDataIndex()
		{
			return dataIndex;
		}

		public void setDataIndex(int dataIndex)
		{
			this.dataIndex = dataIndex;
		}

		public void incrementDataIndex()
		{
			this.dataIndex++;
		}
	}
}
