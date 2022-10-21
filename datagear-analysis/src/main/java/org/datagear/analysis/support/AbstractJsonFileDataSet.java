/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.Reader;
import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.AbstractJsonDataSet.JsonDataSetResource;
import org.datagear.analysis.support.AbstractJsonFileDataSet.JsonFileDataSetResource;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 抽象JSON文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonFileDataSet extends AbstractJsonDataSet<JsonFileDataSetResource>
{
	/** 文件编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;

	public AbstractJsonFileDataSet()
	{
		super();
	}

	public AbstractJsonFileDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractJsonFileDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	protected JsonFileDataSetResource getResource(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws Throwable
	{
		File file = getJsonFile(query);
		return new JsonFileDataSetResource("", getDataJsonPath(), getEncoding(), file.getAbsolutePath(),
				file.lastModified());
	}

	/**
	 * 获取JSON文件。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getJsonFile(DataSetQuery query) throws Throwable;

	/**
	 * JSON文件数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonFileDataSetResource extends JsonDataSetResource
	{
		private static final long serialVersionUID = 1L;

		private String encoding;

		private String filePath;

		private long lastModified;

		public JsonFileDataSetResource()
		{
			super();
		}

		public JsonFileDataSetResource(String resolvedTemplate, String dataJsonPath,
				String encoding, String filePath, long lastModified)
		{
			super(resolvedTemplate, dataJsonPath);
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
			return IOUtil.getReader(file, this.encoding);
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
	}
}
