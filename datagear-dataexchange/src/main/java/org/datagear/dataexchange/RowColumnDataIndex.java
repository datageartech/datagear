/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
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
