/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本值导入服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedTextValueDataImportService<T extends TextValueDataImport>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedTextValueDataImportService()
	{
		super();
	}

	public AbstractDevotedTextValueDataImportService(DatabaseInfoResolver databaseInfoResolver)
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
	 * 创建一个{@linkplain TextValueDataImportContext}。
	 * 
	 * @param impt
	 * @return
	 */
	protected TextValueDataImportContext createTextValueDataImportContext(T impt)
	{
		return new TextValueDataImportContext(new DataFormatContext(impt.getDataFormat()));
	}

	/**
	 * 导入一条数据。
	 * 
	 * @param impt
	 * @param cn
	 * @param st
	 * @param columnInfos
	 * @param columnValues
	 * @param context
	 * @return
	 * @throws DataExchangeException
	 */
	protected boolean importData(T impt, Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<String> columnValues, TextValueDataImportContext context) throws DataExchangeException
	{
		TextValueDataImportListener listener = impt.getListener();

		try
		{
			setImportColumnValues(impt, cn, st, columnInfos, columnValues, context);

			executeImportPreparedStatement(impt, st, context);

			if (listener != null)
				listener.onSuccess(context.getDataIndex());

			return true;
		}
		catch (Throwable t)
		{
			DataExchangeException de = wrapToDataExchangeException(t);

			if (ExceptionResolve.IGNORE.equals(impt.getImportOption().getExceptionResolve()))
			{
				if (listener != null)
					listener.onIgnore(context.getDataIndex(), de);

				return false;
			}
			else
				throw de;
		}
		finally
		{
			context.clearCloseResources();
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
	protected void executeImportPreparedStatement(T impt, PreparedStatement st, TextValueDataImportContext context)
			throws ExecuteDataImportSqlException
	{
		try
		{
			st.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new ExecuteDataImportSqlException(context.getDataIndex(), e);
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
	 * @param context
	 * @throws SetImportColumnValueException
	 */
	protected void setImportColumnValues(T impt, Connection cn, PreparedStatement st, List<ColumnInfo> columnInfos,
			List<String> columnValues, TextValueDataImportContext context) throws SetImportColumnValueException
	{
		DataIndex dataIndex = context.getDataIndex();
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
				setStringParameterValue(cn, st, parameterIndex, sqlType, rawValue, context.getDataFormatContext());
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

					TextValueDataImportListener listener = impt.getListener();
					if (listener != null)
					{
						DataExchangeException de = null;

						if ((e instanceof ParseException) || (e instanceof DecoderException))
							de = new IllegalImportSourceValueException(dataIndex, columnName, rawValue, e);
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
						throw new IllegalImportSourceValueException(dataIndex, columnName, rawValue, e);
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
	 * 文本值数据导入上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextValueDataImportContext extends DataExchangeContext
	{
		private DataFormatContext dataFormatContext;

		private DataIndex dataIndex;

		public TextValueDataImportContext()
		{
			super();
		}

		public TextValueDataImportContext(DataFormatContext dataFormatContext)
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

		public DataIndex getDataIndex()
		{
			return dataIndex;
		}

		public void setDataIndex(DataIndex dataIndex)
		{
			this.dataIndex = dataIndex;
		}
	}
}
