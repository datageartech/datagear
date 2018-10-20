/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

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
