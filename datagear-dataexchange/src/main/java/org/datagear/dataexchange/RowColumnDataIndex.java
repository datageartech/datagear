/*
 * Copyright 2018-2023 datagear.tech
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

/**
 * 行、列数据索引。
 * 
 * @author datagear@163.com
 *
 */
public class RowColumnDataIndex extends DataIndex
{
	private static final long serialVersionUID = 1L;

	private long row;

	private long column;

	public RowColumnDataIndex()
	{
		super();
	}

	public RowColumnDataIndex(long row, long column)
	{
		super();
		this.row = row;
		this.column = column;
	}

	public long getRow()
	{
		return row;
	}

	public void setRow(long row)
	{
		this.row = row;
	}

	public long getColumn()
	{
		return column;
	}

	public void setColumn(long column)
	{
		this.column = column;
	}

	@Override
	public String toString()
	{
		return this.row + ", " + this.column;
	}

	/**
	 * 构建{@linkplain RowColumnDataIndex}。
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public static RowColumnDataIndex valueOf(long row, long column)
	{
		return new RowColumnDataIndex(row, column);
	}
}
