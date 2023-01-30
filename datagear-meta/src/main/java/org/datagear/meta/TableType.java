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

package org.datagear.meta;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.meta.resolver.DBMetaResolver;

/**
 * 表类型。
 * <p>
 * 由于各数据库的表类型命名各不相同，所以这里定义的仅是JDBC中所表述的常用类型。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TableType
{
	public static final String TABLE = "TABLE";

	public static final String VIEW = "VIEW";

	public static final String SYSTEM_TABLE = "SYSTEM TABLE";

	public static final String GLOBAL_TEMPORARY = "GLOBAL TEMPORARY";

	public static final String LOCAL_TEMPORARY = "LOCAL TEMPORARY";

	public static final String ALIAS = "ALIAS";

	public static final String SYNONYM = "SYNONYM";

	/**
	 * 规范化表类型。
	 * 
	 * @param tableType
	 * @return
	 */
	public static String toTableType(String tableType)
	{
		if (TableType.TABLE.equalsIgnoreCase(tableType))
			return TableType.TABLE;
		else if (TableType.VIEW.equalsIgnoreCase(tableType))
			return TableType.VIEW;
		else if (TableType.SYSTEM_TABLE.equalsIgnoreCase(tableType))
			return TableType.SYSTEM_TABLE;
		else if (TableType.GLOBAL_TEMPORARY.equalsIgnoreCase(tableType))
			return TableType.GLOBAL_TEMPORARY;
		else if (TableType.LOCAL_TEMPORARY.equalsIgnoreCase(tableType))
			return TableType.LOCAL_TEMPORARY;
		else if (TableType.ALIAS.equalsIgnoreCase(tableType))
			return TableType.ALIAS;
		else if (TableType.SYNONYM.equalsIgnoreCase(tableType))
			return TableType.SYNONYM;
		else
			return tableType;
	}

	/**
	 * 过滤用户数据表。
	 * 
	 * @param cn
	 * @param dbMetaResolver
	 * @param tables
	 * @return
	 */
	public static List<SimpleTable> filterUserDataTables(Connection cn, DBMetaResolver dbMetaResolver,
			List<SimpleTable> tables)
	{
		List<SimpleTable> re = new ArrayList<>(tables.size());

		for (SimpleTable table : tables)
		{
			if (dbMetaResolver.isUserDataTable(cn, table))
				re.add(table);
		}

		return re;
	}

	/**
	 * 过滤用户数据实体表。
	 * 
	 * @param cn
	 * @param dbMetaResolver
	 * @param tables
	 * @return
	 */
	public static List<SimpleTable> filterUserDataEntityTables(Connection cn, DBMetaResolver dbMetaResolver,
			List<SimpleTable> tables)
	{
		List<SimpleTable> re = new ArrayList<>(tables.size());

		for (SimpleTable table : tables)
		{
			if (dbMetaResolver.isUserDataEntityTable(cn, table))
				re.add(table);
		}

		return re;
	}
}
