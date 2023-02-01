/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.AbstractCsvDataSet.CsvDataSetResource;
import org.datagear.analysis.support.CsvValueDataSet.CsvValueDataSetResource;
import org.datagear.util.IOUtil;

/**
 * CSV值数据集。
 * <p>
 * 此类的{@linkplain #getValue()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CsvValueDataSet extends AbstractCsvDataSet<CsvValueDataSetResource>
{
	/** CSV字符串 */
	private String value = "";

	public CsvValueDataSet()
	{
		super();
	}

	public CsvValueDataSet(String id, String name, String value)
	{
		super(id, name);
		this.value = value;
	}

	public CsvValueDataSet(String id, String name, List<DataSetProperty> properties, String value)
	{
		super(id, name, properties);
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	/**
	 * 设置CSV字符串值，格式为：
	 * 
	 * <pre>
	 * name, value
	 * aaa, 1
	 * bbb, 2
	 * </pre>
	 * 
	 * @param value
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public TemplateResolvedDataSetResult resolve(DataSetQuery query)
			throws DataSetException
	{
		return (TemplateResolvedDataSetResult) super.resolve(query);
	}

	@Override
	protected CsvValueDataSetResource getResource(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws Throwable
	{
		String csv = resolveTemplateCsv(this.value, query);
		return new CsvValueDataSetResource(csv, getNameRow());
	}

	/**
	 * CSV文本数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class CsvValueDataSetResource extends CsvDataSetResource
	{
		private static final long serialVersionUID = 1L;

		public CsvValueDataSetResource()
		{
			super();
		}

		public CsvValueDataSetResource(String resolvedTemplate, int nameRow)
		{
			super(resolvedTemplate, nameRow);
		}

		@Override
		public boolean isIdempotent()
		{
			return true;
		}

		@Override
		public Reader getReader() throws Throwable
		{
			return IOUtil.getReader(super.getResolvedTemplate());
		}

		@Override
		public int hashCode()
		{
			return super.hashCode();
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
			return true;
		}
	}
}
