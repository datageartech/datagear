/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.datagear.dataexchange.DataImport;
import org.datagear.dataexchange.DevotedDataImporter;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象{@linkplain DevotedDataImporter}。
 * <p>
 * 它默认实现了{@linkplain #supports(DataImport)}，并且始终返回{@code true}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataImporter<T extends DataImport> extends DataExchangerSupport
		implements DevotedDataImporter<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextDevotedDataImporter.class);

	public AbstractDevotedDataImporter()
	{
		super();
	}

	@Override
	public boolean supports(T impt)
	{
		return true;
	}

	/**
	 * 执行下一个插入操作。
	 * 
	 * @param impt
	 * @param st
	 * @param insertContext
	 * @throws InsertSqlException
	 */
	protected void executeNextInsert(T impt, PreparedStatement st, InsertContext insertContext)
			throws InsertSqlException
	{
		try
		{
			st.executeUpdate();
		}
		catch (SQLException e)
		{
			InsertSqlException e1 = new InsertSqlException(insertContext.getTable(), insertContext.getDataIndex(), e);

			if (impt.isAbortOnError())
				throw e1;
			else
			{
				if (impt.hasDataImportReporter())
					impt.getDataImportReporter().report(e1);
			}
		}
		finally
		{
			insertContext.incrementDataIndex();

			insertContext.clearCloseResources();
		}
	}

	/**
	 * 设置{@linkplain PreparedStatement}参数{@code null}。
	 * 
	 * @param st
	 * @param parameterIndex
	 * @param sqlType
	 */
	protected void setParameterNull(PreparedStatement st, int parameterIndex, int sqlType)
	{
		try
		{
			st.setNull(parameterIndex, sqlType);
		}
		catch (SQLException e)
		{
			LOGGER.error("set PreparedStatement parameter null for sql type [" + sqlType + "]", e);
		}
	}

	/**
	 * 构建插入预编译SQL语句。
	 * 
	 * @param cn
	 * @param table
	 * @param columnInfos
	 * @return
	 * @throws SQLException
	 */
	protected String buildInsertPreparedSql(Connection cn, String table, ColumnInfo[] columnInfos) throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columnInfos[i].getName()).append(quote);
		}

		sql.append(") VALUES (");

		for (int i = 0; i < columnInfos.length; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append('?');
		}

		sql.append(")");

		return sql.toString();
	}

	/**
	 * 移除{@code null}列信息位置对应的列值。
	 * 
	 * @param rawColumnInfos
	 * @param noNullColumnInfos
	 * @param rawColumnValues
	 * @return
	 */
	protected String[] removeNullColumnInfoValues(ColumnInfo[] rawColumnInfos, ColumnInfo[] noNullColumnInfos,
			String[] rawColumnValues)
	{
		if (noNullColumnInfos == rawColumnInfos || noNullColumnInfos.length == rawColumnInfos.length)
			return rawColumnValues;

		String[] newColumnValues = new String[noNullColumnInfos.length];

		int index = 0;

		for (int i = 0; i < rawColumnInfos.length; i++)
		{
			if (rawColumnInfos[i] == null)
				continue;

			newColumnValues[index++] = rawColumnValues[i];
		}

		return newColumnValues;
	}

	/**
	 * 移除{@linkplain ColumnInfo}数组中的{@code null}元素。
	 * <p>
	 * 如果没有{@code null}元素，将返回原数组。
	 * </p>
	 * 
	 * @param columnInfos
	 * @return
	 */
	protected ColumnInfo[] removeNulls(ColumnInfo[] columnInfos)
	{
		boolean noNull = true;

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (columnInfo == null)
			{
				noNull = false;
				break;
			}
		}

		if (noNull)
			return columnInfos;

		List<ColumnInfo> list = new ArrayList<ColumnInfo>(columnInfos.length);

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (columnInfo != null)
				list.add(columnInfo);
		}

		return list.toArray(new ColumnInfo[list.size()]);
	}

	/**
	 * 获取表指定列信息数组。
	 * <p>
	 * 当指定位置的列不存在时，如果{@code nullIfInexistentColumn}为{@code true}，返回数组对应位置将为{@code null}，
	 * 否则，将立刻抛出{@linkplain ColumnNotFoundException}。
	 * </p>
	 * 
	 * @param cn
	 * @param table
	 * @param columnNames
	 * @param nullIfInexistentColumn
	 * @param databaseInfoResolver
	 * @return
	 * @throws ColumnNotFoundException
	 */
	protected ColumnInfo[] getColumnInfos(Connection cn, String table, String[] columnNames,
			boolean nullIfInexistentColumn, DatabaseInfoResolver databaseInfoResolver) throws ColumnNotFoundException
	{
		ColumnInfo[] columnInfos = new ColumnInfo[columnNames.length];

		ColumnInfo[] allColumnInfos = databaseInfoResolver.getColumnInfos(cn, table);

		for (int i = 0; i < columnNames.length; i++)
		{
			ColumnInfo columnInfo = null;

			for (int j = 0; j < allColumnInfos.length; j++)
			{
				if (allColumnInfos[j].getName().equals(columnNames[i]))
				{
					columnInfo = allColumnInfos[j];
					break;
				}
			}

			if (!nullIfInexistentColumn && columnInfo == null)
				throw new ColumnNotFoundException(table, columnNames[i]);

			columnInfos[i] = columnInfo;
		}

		return columnInfos;
	}

	/**
	 * SQL插入操作上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class InsertContext
	{
		private List<Closeable> closeResources = new LinkedList<Closeable>();

		private String table;

		private int dataIndex = 0;

		public InsertContext()
		{
			super();
		}

		public InsertContext(String table)
		{
			super();
			this.table = table;
		}

		public String getTable()
		{
			return table;
		}

		public void setTable(String table)
		{
			this.table = table;
		}

		public int getDataIndex()
		{
			return dataIndex;
		}

		public void setDataIndex(int dataIndex)
		{
			this.dataIndex = dataIndex;
		}

		/**
		 * 数据索引加{@code 1}。
		 */
		public void incrementDataIndex()
		{
			this.dataIndex += 1;
		}

		/**
		 * 添加一个待关闭的{@linkplain Closeable}。
		 * 
		 * @param closeable
		 */
		public void addCloseResource(Closeable closeable)
		{
			this.closeResources.add(closeable);
		}

		/**
		 * 清除并关闭所有{@linkplain Closeable}。
		 * 
		 * @return
		 */
		public int clearCloseResources()
		{
			int size = closeResources.size();

			for (int i = 0; i < size; i++)
			{
				Closeable closeable = this.closeResources.get(i);

				try
				{
					closeable.close();
				}
				catch (IOException e)
				{
				}
			}

			return size;
		}
	}
}
