/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;

/**
 * {@linkplain ColumnInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class ColumnInfoResultSetSpec extends ResultSetSpec<ColumnInfo>
{
	public static final Converter<Integer, Boolean> NULLABLE_CONVERTER = new Converter<Integer, Boolean>()
	{
		@Override
		public Boolean convert(Integer s) throws ResultSetIncompatibleException
		{
			if (DatabaseMetaData.columnNoNulls == s)
				return false;
			else
				return true;
		}
	};

	public static final Converter<String, Boolean> IS_AUTOINCREMENT_CONVERTER = new Converter<String, Boolean>()
	{
		@Override
		public Boolean convert(String s) throws ResultSetIncompatibleException
		{
			return "yes".equalsIgnoreCase(s);
		}
	};

	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec[] {
			new RsColumnSpec<String, String>("COLUMN_NAME", String.class, true, false, "name"),
			new RsColumnSpec<Integer, Integer>("DATA_TYPE", Integer.class, true, false, "type"),
			new RsColumnSpec<String, String>("TYPE_NAME", String.class, true, false, "typeName"),
			new RsColumnSpec<Integer, Integer>("COLUMN_SIZE", Integer.class, false, true, 0, "size"),
			new RsColumnSpec<Integer, Integer>("DECIMAL_DIGITS", Integer.class, false, true, 0, "decimalDigits"),
			new RsColumnSpec<Integer, Boolean>("NULLABLE", Integer.class, false, true, "nullable", NULLABLE_CONVERTER),
			new RsColumnSpec<String, String>("REMARKS", String.class, false, true, "", "comment"),
			new RsColumnSpec<String, String>("COLUMN_DEF", String.class, false, true, "defaultValue"),
			new RsColumnSpec<String, Boolean>("IS_AUTOINCREMENT", String.class, false, true, "autoincrement",
					IS_AUTOINCREMENT_CONVERTER) };

	public ColumnInfoResultSetSpec()
	{
		super();
	}

	@Override
	protected Class<ColumnInfo> getRowType()
	{
		return ColumnInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
