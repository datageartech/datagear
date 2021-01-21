/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
