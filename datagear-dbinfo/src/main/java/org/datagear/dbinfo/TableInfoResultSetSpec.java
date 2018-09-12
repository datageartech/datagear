/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

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
