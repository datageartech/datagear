/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.meta;

import org.datagear.util.JDBCCompatiblity;

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

	/** 索引，PostgreSQL-9.6中存在此类型 */
	public static final String INDEX = "INDEX";

	/** 序列，PostgreSQL-9.6中存在此类型 */
	public static final String SEQUENCE = "SEQUENCE";

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
		else if (TableType.INDEX.equalsIgnoreCase(tableType))
			return TableType.INDEX;
		else if (TableType.SEQUENCE.equalsIgnoreCase(tableType))
			return TableType.SEQUENCE;
		else
			return tableType;
	}

	/**
	 * 是否是用户数据表类型。
	 * 
	 * @param tableType
	 * @return
	 */
	@JDBCCompatiblity("每个数据库都有各自不同的表类型命名，所以这里只能是尽量排除")
	public static boolean isUserDataTableType(String tableType)
	{
		if (SYSTEM_TABLE.equalsIgnoreCase(tableType) || GLOBAL_TEMPORARY.equalsIgnoreCase(tableType)
				|| LOCAL_TEMPORARY.equalsIgnoreCase(tableType) || INDEX.equalsIgnoreCase(tableType)
				|| SEQUENCE.equalsIgnoreCase(tableType))
			return false;

		return true;
	}

	/**
	 * 是否是用户数据表实体类型。
	 * 
	 * @param tableType
	 * @return
	 */
	@JDBCCompatiblity("每个数据库都有各自不同的表类型命名，所以这里只能是尽量排除")
	public static boolean isUserDataEntityTableType(String tableType)
	{
		if (!isUserDataTableType(tableType))
			return false;

		if (VIEW.equalsIgnoreCase(tableType) || ALIAS.equalsIgnoreCase(tableType)
				|| SYNONYM.equalsIgnoreCase(tableType))
			return false;

		return true;
	}
}
