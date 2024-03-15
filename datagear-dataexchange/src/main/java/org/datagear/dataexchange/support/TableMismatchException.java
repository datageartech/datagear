/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataExchangeException;

/**
 * 表不匹配异常。
 * <p>
 * 导入数据时，在给定表中没有找到任何匹配列。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TableMismatchException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private String table;

	public TableMismatchException(String table)
	{
		super();
		this.table = table;
	}

	public TableMismatchException(String table, String message)
	{
		super(message);
		this.table = table;
	}

	public TableMismatchException(String table, Throwable cause)
	{
		super(cause);
		this.table = table;
	}

	public TableMismatchException(String table, String message, Throwable cause)
	{
		super(message, cause);
		this.table = table;
	}

	public String getTable()
	{
		return table;
	}

	protected void setTable(String table)
	{
		this.table = table;
	}
}
