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
