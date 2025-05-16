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
import java.io.Reader;

import org.datagear.util.FileUtil;

/**
 * JSON文件数据集资源。
 * 
 * @author datagear@163.com
 *
 */
public class JsonFileDataSetResource extends JsonDataSetResource
{
	private static final long serialVersionUID = 1L;

	private String encoding;

	private String filePath;

	private long lastModified;

	public JsonFileDataSetResource()
	{
		super();
	}

	public JsonFileDataSetResource(String resolvedTemplate, String dataJsonPath, String additionDataProp,
			String encoding, String filePath, long lastModified)
	{
		super(resolvedTemplate, dataJsonPath, additionDataProp);
		this.filePath = filePath;
		this.lastModified = lastModified;
		this.encoding = encoding;
	}

	public String getFilePath()
	{
		return filePath;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public String getEncoding()
	{
		return encoding;
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}

	@Override
	public Reader getReader() throws Throwable
	{
		File file = FileUtil.getFile(this.filePath);
		return getReader(file, this.encoding);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
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
		JsonFileDataSetResource other = (JsonFileDataSetResource) obj;
		if (encoding == null)
		{
			if (other.encoding != null)
				return false;
		}
		else if (!encoding.equals(other.encoding))
			return false;
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
		return getClass().getSimpleName() + " [encoding=" + encoding + ", filePath=" + filePath + ", lastModified="
				+ lastModified + ", dataJsonPath=" + getDataJsonPath() + ", additionDataProp=" + getAdditionDataProp()
				+ ", resolvedTemplate=" + getResolvedTemplate() + "]";
	}
}