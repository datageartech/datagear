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

package org.datagear.web.util.msg;

/**
 * 数据表消息。
 * 
 * @author datagear@163.com
 *
 */
public class TableMessage extends TextMessage
{
	private static final long serialVersionUID = 1L;

	private String tableName = "";

	public TableMessage()
	{
		super();
	}

	public TableMessage(String tableName)
	{
		super();
		this.tableName = tableName;
	}

	public TableMessage(String tableName, String content)
	{
		super(content);
		this.tableName = tableName;
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
}
