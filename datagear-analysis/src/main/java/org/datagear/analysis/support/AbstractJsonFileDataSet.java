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
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 抽象JSON文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonFileDataSet extends AbstractJsonDataSet
{
	private static final long serialVersionUID = 1L;

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
	protected JsonTemplateResolvedResource getJsonResource(DataSetQuery query) throws Throwable
	{
		File file = getJsonFile(query);
		return new JsonFileTemplateResolvedResource(getDataJsonPath(), file.getAbsolutePath(), file.lastModified(),
				getEncoding());
	}

	/**
	 * 获取JSON文件。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getJsonFile(DataSetQuery query) throws Throwable;

	protected static class JsonFileTemplateResolvedResource extends JsonTemplateResolvedResource
	{
		private static final long serialVersionUID = 1L;

		private final String filePath;

		private final long lastModified;

		private final String encoding;

		public JsonFileTemplateResolvedResource(String dataJsonPath, String filePath, long lastModified,
				String encoding)
		{
			super("", dataJsonPath);
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
		public Reader getResource() throws Throwable
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
			JsonFileTemplateResolvedResource other = (JsonFileTemplateResolvedResource) obj;
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
