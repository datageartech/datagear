/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.util.List;

import org.datagear.util.JDBCCompatiblity;

/**
 * {@linkplain TableInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class TableInfoResultSetSpec extends ResultSetSpec<TableInfo>
{
	public static final String TABLE_TYPE_TABLE = "TABLE";

	public static final String TABLE_TYPE_VIEW = "VIEW";

	public static final String TABLE_TYPE_ALIAS = "ALIAS";

	public static final Converter<String, TableType> TABLE_TYPE_CONVERTER = new Converter<String, TableType>()
	{
		@Override
		public TableType convert(String type) throws ResultSetIncompatibleException
		{
			if (TABLE_TYPE_TABLE.equals(type))
				return TableType.TABLE;
			else if (TABLE_TYPE_VIEW.equals(type))
				return TableType.VIEW;
			else if (TABLE_TYPE_ALIAS.equals(type))
				return TableType.ALIAS;
			else
				return TableType.TABLE;
		}
	};

	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec<?, ?>[] {
			new RsColumnSpec<String, String>("TABLE_NAME", String.class, true, false, "name"),
			new RsColumnSpec<String, TableType>("TABLE_TYPE", String.class, false, true, "type", TABLE_TYPE_CONVERTER),
			new RsColumnSpec<String, TableType>("REMARKS", String.class, false, true, "", "comment") };

	public TableInfoResultSetSpec()
	{
		super();
	}

	@Override
	@JDBCCompatiblity("避免某些驱动程序的结果集出现重复项")
	protected void addToList(List<TableInfo> list, TableInfo bean)
	{
		for (TableInfo ele : list)
		{
			if (equalsWithNull(ele.getName(), bean.getName()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<TableInfo> getRowType()
	{
		return TableInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
