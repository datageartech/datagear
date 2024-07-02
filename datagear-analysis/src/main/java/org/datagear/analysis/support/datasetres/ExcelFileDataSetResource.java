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

import java.io.File;
import java.io.InputStream;

import org.datagear.util.FileUtil;

/**
 * Excel文件数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelFileDataSetResource extends ExcelDataSetResource
{
	private static final long serialVersionUID = 1L;

	private String filePath;

	private long lastModified;

	public ExcelFileDataSetResource()
	{
		super();
	}

	public ExcelFileDataSetResource(String resolvedTemplate, String sheetName, int sheetIndex, int nameRow, String dataRowExp,
			String dataColumnExp, boolean xls, String filePath, long lastModified)
	{
		super(resolvedTemplate, sheetName, sheetIndex, nameRow, dataRowExp, dataColumnExp, xls);
		this.filePath = filePath;
		this.lastModified = lastModified;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
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
	public InputStream getInputStream() throws Throwable
	{
		File file = FileUtil.getFile(this.filePath);
		return getInputStream(file);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
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
		if (filePath == null)
		{
			if (other.filePath != null)
				return false;
		}
		else if (!filePath.equals(other.filePath))
			return false;
		if (lastModified != other.lastModified)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [filePath=" + filePath + ", lastModified=" + lastModified
				+ ", sheetName=" + getSheetName() + ", sheetIndex=" + getSheetIndex() + ", nameRow=" + getNameRow()
				+ ", dataRowExp=" + getDataRowExp() + ", dataColumnExp=" + getDataColumnExp() + ", xls=" + isXls()
				+ ", resolvedTemplate=" + getResolvedTemplate() + "]";
	}
}