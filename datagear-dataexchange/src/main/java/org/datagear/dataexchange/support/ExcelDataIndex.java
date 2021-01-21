/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

import org.datagear.dataexchange.DataIndex;

/**
 * Excel数据索引。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataIndex extends DataIndex
{
	private static final long serialVersionUID = 1L;

	private int sheetIndex;

	private int row;

	public ExcelDataIndex()
	{
		super();
	}

	public ExcelDataIndex(int sheetIndex, int row)
	{
		super();
		this.sheetIndex = sheetIndex;
		this.row = row;
	}

	public int getSheetIndex()
	{
		return sheetIndex;
	}

	public void setSheetIndex(int sheetIndex)
	{
		this.sheetIndex = sheetIndex;
	}

	public int getRow()
	{
		return row;
	}

	public void setRow(int row)
	{
		this.row = row;
	}

	@Override
	public String toString()
	{
		return this.sheetIndex + ", " + this.row;
	}

	/**
	 * 构建{@linkplain ExcelDataIndex}。
	 * 
	 * @param sheetIndex
	 * @param row
	 * @return
	 */
	public static ExcelDataIndex valueOf(int sheetIndex, int row)
	{
		return new ExcelDataIndex(sheetIndex, row);
	}
}
