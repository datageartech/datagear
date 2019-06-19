/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DevotedDataExchangeService;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象{@linkplain DevotedDataExchangeService}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataExchangeService<T extends DataExchange>
		implements DevotedDataExchangeService<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDevotedDataExchangeService.class);

	public AbstractDevotedDataExchangeService()
	{
		super();
	}

	@Override
	public boolean supports(T dataExchange)
	{
		return true;
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
			cn.rollback();
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
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
			cn.commit();
		}
		catch (SQLException e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 静默回滚。
	 * 
	 * @param cn
	 */
	protected void rollbackSilently(Connection cn)
	{
		try
		{
			cn.rollback();
		}
		catch (Throwable t)
		{
			LOGGER.error("rollback connection exception", t);
		}
	}

	/**
	 * 静默提交。
	 * 
	 * @param cn
	 */
	protected void commitSilently(Connection cn)
	{
		try
		{
			cn.commit();
		}
		catch (Throwable t)
		{
			LOGGER.error("commit connection exception", t);
		}
	}

	/**
	 * 获取资源。
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @return
	 * @throws DataExchangeException
	 */
	protected <R> R getResource(ResourceFactory<R> resourceFactory) throws DataExchangeException
	{
		try
		{
			return resourceFactory.get();
		}
		catch (Exception e)
		{
			throw new DataExchangeException(e);
		}
	}

	/**
	 * 释放资源。
	 * <p>
	 * 此方法不会抛出任何{@linkplain Throwable}。
	 * </p>
	 * 
	 * @param <R>
	 * @param resourceFactory
	 * @param resource
	 */
	protected <R> void releaseResource(ResourceFactory<R> resourceFactory, R resource)
	{
		if (resource == null)
			return;

		try
		{
			resourceFactory.release(resource);
		}
		catch (Throwable e)
		{
			LOGGER.error("release connection error", e);
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
			throw new DataExchangeException(t);
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
	protected String buildInsertPreparedSql(Connection cn, String table, List<ColumnInfo> columnInfos)
			throws SQLException
	{
		String quote = cn.getMetaData().getIdentifierQuoteString();

		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(quote).append(table).append(quote);
		sql.append(" (");

		int size = columnInfos.size();

		for (int i = 0; i < size; i++)
		{
			if (i != 0)
				sql.append(',');

			sql.append(quote).append(columnInfos.get(i).getName()).append(quote);
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
	 * 移除{@code null}列信息位置对应的列值。
	 * <p>
	 * 如果没有{@code null}列信息，将返回原列值列表。
	 * </p>
	 * 
	 * @param rawColumnInfos
	 * @param noNullColumnInfos
	 * @param columnValues
	 * @return
	 */
	protected <G> List<G> removeNullColumnValues(List<ColumnInfo> rawColumnInfos, List<ColumnInfo> noNullColumnInfos,
			List<G> columnValues)
	{
		if (noNullColumnInfos == rawColumnInfos || noNullColumnInfos.size() == rawColumnInfos.size())
			return columnValues;

		List<G> newColumnValues = new ArrayList<G>(noNullColumnInfos.size());

		for (G ele : columnValues)
		{
			if (ele == null)
				continue;

			newColumnValues.add(ele);
		}

		return newColumnValues;
	}

	/**
	 * 移除{@linkplain ColumnInfo}列表中的{@code null}元素。
	 * <p>
	 * 如果没有{@code null}元素，将返回原列表。
	 * </p>
	 * 
	 * @param columnInfos
	 * @return
	 */
	protected List<ColumnInfo> removeNullColumnInfos(List<ColumnInfo> columnInfos)
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

		List<ColumnInfo> list = new ArrayList<ColumnInfo>(columnInfos.size());

		for (ColumnInfo columnInfo : columnInfos)
		{
			if (columnInfo != null)
				list.add(columnInfo);
		}

		return list;
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
	 * @param databaseInfoResolver
	 * @return
	 * @throws TableNotFoundException
	 * @throws ColumnNotFoundException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, String table, List<String> columnNames,
			boolean nullIfColumnNotFound, DatabaseInfoResolver databaseInfoResolver)
			throws TableNotFoundException, ColumnNotFoundException
	{
		int size = columnNames.size();

		List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>(size);

		ColumnInfo[] allColumnInfos = databaseInfoResolver.getColumnInfos(cn, table);

		if (allColumnInfos == null || allColumnInfos.length == 0)
			throw new TableNotFoundException(table);

		for (int i = 0; i < size; i++)
		{
			String columnName = columnNames.get(i);

			ColumnInfo columnInfo = null;

			for (int j = 0; j < allColumnInfos.length; j++)
			{
				if (allColumnInfos[j].getName().equals(columnName))
				{
					columnInfo = allColumnInfos[j];
					break;
				}
			}

			if (!nullIfColumnNotFound && columnInfo == null)
				throw new ColumnNotFoundException(table, columnName);

			columnInfos.add(columnInfo);
		}

		return columnInfos;
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @param databaseInfoResolver
	 * @return
	 * @throws SQLException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, ResultSet rs, DatabaseInfoResolver databaseInfoResolver)
			throws SQLException
	{
		ResultSetMetaData resultSetMetaData = rs.getMetaData();
		ColumnInfo[] columnInfos = databaseInfoResolver.getColumnInfos(cn, resultSetMetaData);

		List<ColumnInfo> list = new ArrayList<ColumnInfo>(columnInfos.length);

		for (ColumnInfo columnInfo : columnInfos)
			list.add(columnInfo);

		return list;
	}
}
