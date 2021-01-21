/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 行号数据索引。
 * 
 * @author datagear@163.com
 *
 */
public class RowDataIndex extends DataIndex
{
	private static final long serialVersionUID = 1L;

	private long row = 0;

	public RowDataIndex()
	{
		super();
	}

	public RowDataIndex(long row)
	{
		super();
		this.row = row;
	}

	public long getRow()
	{
		return row;
	}

	public void setRow(long row)
	{
		this.row = row;
	}

	@Override
	public String toString()
	{
		return Long.toString(this.row);
	}

	/**
	 * 构建{@linkplain RowDataIndex}。
	 * 
	 * @param row
	 * @return
	 */
	public static RowDataIndex valueOf(long row)
	{
		return new RowDataIndex(row);
	}
}
