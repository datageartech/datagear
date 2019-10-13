/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.DatabaseMetaData;
import java.util.List;

import org.datagear.dbinfo.SqlTypeInfo.SearchableType;
import org.datagear.util.JDBCCompatiblity;

/**
 * {@linkplain SqlTypeInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTypeInfoResultSetSpec extends ResultSetSpec<SqlTypeInfo>
{
	public static final Converter<Integer, SearchableType> SEARCHABLETYPE_CONVERTER = new Converter<Integer, SearchableType>()
	{
		@Override
		public SearchableType convert(Integer type) throws ResultSetIncompatibleException
		{
			if (DatabaseMetaData.typePredNone == type)
				return SearchableType.NO;
			else if (DatabaseMetaData.typePredChar == type)
				return SearchableType.ONLY_LIKE;
			else if (DatabaseMetaData.typePredBasic == type)
				return SearchableType.EXPCEPT_LIKE;
			else if (DatabaseMetaData.typeSearchable == type)
				return SearchableType.ALL;
			else
				return SearchableType.NO;
		}
	};

	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec<?, ?>[] {
			new RsColumnSpec<String, String>("TYPE_NAME", String.class, true, false, "name"),
			new RsColumnSpec<Integer, Integer>("DATA_TYPE", Integer.class, false, true, "type"),
			new RsColumnSpec<Integer, SearchableType>("SEARCHABLE", Integer.class, true, true, "searchableType",
					SEARCHABLETYPE_CONVERTER) };

	public SqlTypeInfoResultSetSpec()
	{
		super();
	}

	@Override
	@JDBCCompatiblity("避免某些驱动程序的结果集出现重复项")
	protected void addToList(List<SqlTypeInfo> list, SqlTypeInfo bean)
	{
		for (SqlTypeInfo ele : list)
		{
			if (equalsWithNull(ele.getName(), bean.getName()) || equalsWithNull(ele.getType(), bean.getType()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<SqlTypeInfo> getRowType()
	{
		return SqlTypeInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
