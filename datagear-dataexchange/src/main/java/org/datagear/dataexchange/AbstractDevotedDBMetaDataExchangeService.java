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
import org.datagear.meta.Table;
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

	protected Table getTableIfValid(Connection cn, String table) throws TableNotFoundException
	{
		return super.getTableIfValid(cn, table, this.dbMetaResolver);
	}
}
