/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.util.List;

import org.datagear.util.JDBCCompatiblity;

/**
 * {@linkplain PrimaryKeyInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class PrimaryKeyInfoResultSetSpec extends ResultSetSpec<PrimaryKeyInfo>
{
	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec[] {
			new RsColumnSpec<String, String>("COLUMN_NAME", String.class, true, false, "columnName") };

	public PrimaryKeyInfoResultSetSpec()
	{
		super();
	}

	@Override
	@JDBCCompatiblity("避免某些驱动程序的结果集出现重复项")
	protected void addToList(List<PrimaryKeyInfo> list, PrimaryKeyInfo bean)
	{
		for (PrimaryKeyInfo ele : list)
		{
			if (equalsWithNull(ele.getColumnName(), bean.getColumnName()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<PrimaryKeyInfo> getRowType()
	{
		return PrimaryKeyInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
