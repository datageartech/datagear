/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
