/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.TextDataExportOption;

/**
 * SQL导出设置项。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExportOption extends TextDataExportOption
{
	private static final long serialVersionUID = 1L;

	private boolean exportCreationSql = false;

	public SqlDataExportOption()
	{
		super();
	}

	public SqlDataExportOption(boolean nullForIllegalColumnValue, boolean exportCreationSql)
	{
		super(nullForIllegalColumnValue);
		this.exportCreationSql = exportCreationSql;
	}

	public boolean isExportCreationSql()
	{
		return exportCreationSql;
	}

	public void setExportCreationSql(boolean exportCreationSql)
	{
		this.exportCreationSql = exportCreationSql;
	}
}
