/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.util.List;

import org.datagear.util.JDBCCompatiblity;

/**
 * {@linkplain ExportedKeyInfo}结果集规范。
 * 
 * @author datagear@163.com
 *
 */
public class ExportedKeyInfoResultSetSpec extends ResultSetSpec<ExportedKeyInfo>
{
	public static final RsColumnSpec<?, ?>[] RS_COLUMN_SPECS = new RsColumnSpec[] {
			new RsColumnSpec<String, String>("PKCOLUMN_NAME", String.class, true, false, "pkColumnName"),
			new RsColumnSpec<String, String>("FKTABLE_NAME", String.class, true, false, "fkTableName"),
			new RsColumnSpec<String, String>("FKCOLUMN_NAME", String.class, true, false, "fkColumnName"),
			new RsColumnSpec<Integer, Integer>("KEY_SEQ", Integer.class, false, true, 0, "keySeq"),
			new RsColumnSpec<Integer, ImportedKeyRule>("UPDATE_RULE", Integer.class, false, true, "updateRule",
					ImportedKeyInfoResultSetSpec.IMPORTED_KEY_RULE_CONVERTER),
			new RsColumnSpec<Integer, ImportedKeyRule>("DELETE_RULE", Integer.class, false, true, "deleteRule",
					ImportedKeyInfoResultSetSpec.IMPORTED_KEY_RULE_CONVERTER),
			new RsColumnSpec<String, String>("FK_NAME", String.class, false, true, "", "fkName"),
			new RsColumnSpec<String, String>("PK_NAME", String.class, false, true, "", "pkName") };

	public ExportedKeyInfoResultSetSpec()
	{
		super();
	}

	@Override
	@JDBCCompatiblity("避免某些驱动程序的结果集出现重复项")
	protected void addToList(List<ExportedKeyInfo> list, ExportedKeyInfo bean)
	{
		for (ExportedKeyInfo ele : list)
		{
			if (equalsWithNull(ele.getFkColumnName(), bean.getFkColumnName())
					&& equalsWithNull(ele.getFkTableName(), bean.getFkTableName())
					&& equalsWithNull(ele.getPkColumnName(), bean.getPkColumnName()))
				return;
		}

		list.add(bean);
	}

	@Override
	protected Class<ExportedKeyInfo> getRowType()
	{
		return ExportedKeyInfo.class;
	}

	@Override
	protected RsColumnSpec<?, ?>[] getRsColumnSpecs()
	{
		return RS_COLUMN_SPECS;
	}
}
