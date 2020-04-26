/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
