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

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.datagear.meta.Column;
import org.datagear.meta.resolver.DBMetaResolver;

/**
 * 抽象导入服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDBMetaDataExchangeService<T extends DataExchange>
		extends AbstractDevotedDataExchangeService<T>
{
	private DBMetaResolver dbMetaResolver;

	public AbstractDevotedDBMetaDataExchangeService()
	{
		super();
	}

	public AbstractDevotedDBMetaDataExchangeService(DBMetaResolver dbMetaResolver)
	{
		super();
		this.dbMetaResolver = dbMetaResolver;
	}

	public DBMetaResolver getDbMetaResolver()
	{
		return dbMetaResolver;
	}

	public void setDbMetaResolver(DBMetaResolver dbMetaResolver)
	{
		this.dbMetaResolver = dbMetaResolver;
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<Column> getColumns(Connection cn, ResultSet rs) throws SQLException
	{
		return super.getColumns(cn, rs, this.dbMetaResolver);
	}

	/**
	 * 获取表所有列信息。
	 * 
	 * @param cn
	 * @param table
	 * @return
	 * @throws TableNotFoundException
	 */
	protected List<Column> getColumns(Connection cn, String table) throws TableNotFoundException
	{
		return super.getColumns(cn, table, this.dbMetaResolver);
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
	protected List<Column> getColumns(Connection cn, String table, List<String> columnNames,
			boolean nullIfColumnNotFound) throws TableNotFoundException, ColumnNotFoundException
	{
		return getColumns(cn, table, columnNames, nullIfColumnNotFound, this.dbMetaResolver);
	}
}
