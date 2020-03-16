/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.util.List;

import org.datagear.util.JDBCCompatiblity;

/**
 * {@linkplain UniqueKeyInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class UniqueKeyInfoResultSetSpec extends ResultSetSpec<UniqueKeyInfo>
{
	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec[] {
			new RsColumnSpec<String, String>("INDEX_NAME", String.class, false, true, "", "keyName"),
			new RsColumnSpec<String, String>("COLUMN_NAME", String.class, true, false, "columnName") };

	public UniqueKeyInfoResultSetSpec()
	{
		super();
	}

	@Override
	@JDBCCompatiblity("避免某些驱动程序的结果集出现重复项")
	protected void addToList(List<UniqueKeyInfo> list, UniqueKeyInfo bean)
	{
		for (UniqueKeyInfo ele : list)
		{
			if (equalsWithNull(ele.getColumnName(), bean.getColumnName()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<UniqueKeyInfo> getRowType()
	{
		return UniqueKeyInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
