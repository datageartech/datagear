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

/**
 * Excel文件数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelFileDataSetResource extends ExcelDataSetResource
{
	private static final long serialVersionUID = 1L;

	private String absolutePath;

	private long lastModified;

	public ExcelFileDataSetResource()
	{
		super();
	}

	public ExcelFileDataSetResource(String resolvedTemplate, String sheetName, int sheetIndex, int nameRow, String dataRowExp,
			String dataColumnExp, boolean xls, String absolutePath, long lastModified)
	{
		super(resolvedTemplate, sheetName, sheetIndex, nameRow, dataRowExp, dataColumnExp, xls);
		this.absolutePath = absolutePath;
		this.lastModified = lastModified;
	}

	public String getAbsolutePath()
	{
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath)
	{
		this.absolutePath = absolutePath;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
		result = prime * result + (int) (lastModified ^ (lastModified >>> 32));
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
		ExcelFileDataSetResource other = (ExcelFileDataSetResource) obj;
		if (absolutePath == null)
		{
			if (other.absolutePath != null)
				return false;
		}
		else if (!absolutePath.equals(other.absolutePath))
			return false;
		if (lastModified != other.lastModified)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [absolutePath=" + absolutePath + ", lastModified=" + lastModified
				+ ", sheetName=" + getSheetName() + ", sheetIndex=" + getSheetIndex() + ", nameRow=" + getNameRow()
				+ ", dataRowExp=" + getDataRowExp() + ", dataColumnExp=" + getDataColumnExp() + ", xls=" + isXls()
				+ ", resolvedTemplate=" + getResolvedTemplate() + "]";
	}
}