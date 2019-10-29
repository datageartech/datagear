/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象导入服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDbInfoAwareDataExchangeService<T extends DataExchange>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedDbInfoAwareDataExchangeService()
	{
		super();
	}

	public AbstractDevotedDbInfoAwareDataExchangeService(DatabaseInfoResolver databaseInfoResolver)
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
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, ResultSet rs) throws SQLException
	{
		return super.getColumnInfos(cn, rs, this.databaseInfoResolver);
	}

	/**
	 * 获取表所有列信息。
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws TableNotFoundException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, String table) throws TableNotFoundException
	{
		return super.getColumnInfos(cn, table, this.databaseInfoResolver);
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
}
