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

package org.datagear.analysis.support.datasetres;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.support.DataSetSourceParseException;
import org.datagear.analysis.support.RangeExpResolver;
import org.datagear.analysis.support.RangeExpResolver.IndexRange;
import org.datagear.analysis.support.RangeExpResolver.Range;
import org.datagear.util.StringUtil;

/**
 * Excel数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public abstract class ExcelDataSetResource extends DataSetResource
{
	protected static final RangeExpResolver RANGE_EXP_RESOLVER = RangeExpResolver
			.valueOf(RangeExpResolver.RANGE_SPLITTER_CHAR, RangeExpResolver.RANGE_GROUP_SPLITTER_CHAR);

	private static final long serialVersionUID = 1L;

	private String sheetName;
	
	private int sheetIndex;

	private int nameRow;

	private String dataRowExp;

	private String dataColumnExp;

	private boolean xls;

	private transient List<IndexRange> _dataRowRanges = null;
	private transient List<IndexRange> _dataColumnRanges = null;

	public ExcelDataSetResource()
	{
		super();
	}

	public ExcelDataSetResource(String resolvedTemplate, String sheetName, int sheetIndex, int nameRow, String dataRowExp,
			String dataColumnExp, boolean xls)
	{
		super(resolvedTemplate);
		this.sheetName = sheetName;
		this.sheetIndex = sheetIndex;
		this.nameRow = nameRow;
		this.dataRowExp = dataRowExp;
		this.dataColumnExp = dataColumnExp;
		this.xls = xls;

		this._dataRowRanges = getRangeExpResolver().resolveIndex(this.dataRowExp);
		this._dataColumnRanges = resolveDataColumnRanges(dataColumnExp);
	}

	public String getSheetName()
	{
		return sheetName;
	}

	public int getSheetIndex()
	{
		return sheetIndex;
	}

	public int getNameRow()
	{
		return nameRow;
	}

	public String getDataRowExp()
	{
		return dataRowExp;
	}

	public String getDataColumnExp()
	{
		return dataColumnExp;
	}

	public boolean isXls()
	{
		return xls;
	}
	
	/**
	 * 获取数据所在工作表。
	 * 
	 * @param wb
	 * @return
	 * @throws DataSetException
	 */
	public Sheet getDataSheet(Workbook wb) throws DataSetException
	{
		Sheet sheet = null;
		
		//sheet名应优先使用
		if(!StringUtil.isEmpty(this.sheetName))
			sheet = wb.getSheet(this.sheetName);
		else
		{
			int sheetIndex = (this.sheetIndex < 1 ? 0 : this.sheetIndex - 1);
			sheet = wb.getSheetAt(sheetIndex);
		}
		
		if(sheet == null)
			throw new DataSetSourceParseException("No sheet found");
		
		return sheet;
	}
	
	/**
	 * 获取Excel输入流。
	 * 
	 * @return
	 * @throws Throwable
	 */
	public abstract InputStream getInputStream() throws Throwable;

	/**
	 * 是否名称行。
	 * 
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isNameRow(int rowIndex)
	{
		return ((rowIndex + 1) == this.nameRow);
	}

	/**
	 * 是否在名称行之后。
	 * <p>
	 * 如果没有名称行，应返回{@code true}。
	 * </p>
	 * 
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isAfterNameRow(int rowIndex)
	{
		return ((rowIndex + 1) > this.nameRow);
	}

	/**
	 * 是否数据行。
	 * 
	 * @param rowIndex 行索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isDataRow(int rowIndex)
	{
		if (isNameRow(rowIndex))
			return false;

		if (this._dataRowRanges == null || this._dataRowRanges.isEmpty())
			return true;

		return IndexRange.includes(this._dataRowRanges, rowIndex + 1);
	}

	/**
	 * 是否数据列。
	 * 
	 * @param columnIndex 列索引（以{@code 0}计数）
	 * @return
	 */
	public boolean isDataColumn(int columnIndex)
	{
		if (this._dataColumnRanges == null || this._dataColumnRanges.isEmpty())
			return true;

		return IndexRange.includes(this._dataColumnRanges, columnIndex);
	}

	@SuppressWarnings("unchecked")
	protected List<IndexRange> resolveDataColumnRanges(String dataColumnExp) throws DataSetException
	{
		List<Range> ranges = getRangeExpResolver().resolve(dataColumnExp);

		if (ranges == null || ranges.isEmpty())
			return Collections.EMPTY_LIST;

		List<IndexRange> indexRanges = new ArrayList<>(ranges.size());

		for (Range range : ranges)
		{
			int from = 0;
			int to = -1;

			String fromStr = range.trimFrom();
			String toStr = range.trimTo();

			if (!StringUtil.isEmpty(fromStr))
				from = CellReference.convertColStringToIndex(fromStr);

			if (!StringUtil.isEmpty(toStr))
				to = CellReference.convertColStringToIndex(toStr);

			indexRanges.add(new IndexRange(from, to));
		}

		return indexRanges;
	}

	protected RangeExpResolver getRangeExpResolver()
	{
		return RANGE_EXP_RESOLVER;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dataColumnExp == null) ? 0 : dataColumnExp.hashCode());
		result = prime * result + ((dataRowExp == null) ? 0 : dataRowExp.hashCode());
		result = prime * result + nameRow;
		result = prime * result + sheetIndex;
		result = prime * result + ((sheetName == null) ? 0 : sheetName.hashCode());
		result = prime * result + (xls ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExcelDataSetResource other = (ExcelDataSetResource) obj;
		if (dataColumnExp == null)
		{
			if (other.dataColumnExp != null)
				return false;
		}
		else if (!dataColumnExp.equals(other.dataColumnExp))
			return false;
		if (dataRowExp == null)
		{
			if (other.dataRowExp != null)
				return false;
		}
		else if (!dataRowExp.equals(other.dataRowExp))
			return false;
		if (nameRow != other.nameRow)
			return false;
		if (sheetIndex != other.sheetIndex)
			return false;
		if (sheetName == null)
		{
			if (other.sheetName != null)
				return false;
		}
		else if (!sheetName.equals(other.sheetName))
			return false;
		if (xls != other.xls)
			return false;
		return true;
	}
}