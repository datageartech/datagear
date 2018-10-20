/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

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
