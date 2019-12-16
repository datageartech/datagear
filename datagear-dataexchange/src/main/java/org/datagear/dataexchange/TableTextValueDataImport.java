/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 单表导入。
 * 
 * @author datagear@163.com
 *
 */
public abstract class TableTextValueDataImport extends TextValueDataImport
{
	/** 要导入的表名 */
	private String table;

	public TableTextValueDataImport()
	{
		super();
	}

	public TableTextValueDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption, String table)
	{
		super(connectionFactory, dataFormat, importOption);
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}
}
