/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.AbstractJsonDataSet.JsonDataSetResource;
import org.datagear.analysis.support.JsonValueDataSet.JsonValueDataSetResource;
import org.datagear.util.IOUtil;

/**
 * JSON字符串值数据集。
 * <p>
 * 此类的{@linkplain #getValue()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class JsonValueDataSet extends AbstractJsonDataSet<JsonValueDataSetResource>
{
	private String value;

	public JsonValueDataSet()
	{
		super();
	}

	public JsonValueDataSet(String id, String name, String value)
	{
		super(id, name);
		this.value = value;
	}

	public JsonValueDataSet(String id, String name, List<DataSetProperty> properties, String value)
	{
		super(id, name, properties);
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

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
	protected JsonValueDataSetResource getResource(DataSetQuery query, List<DataSetProperty> properties,
			boolean resolveProperties) throws Throwable
	{
		String json = resolveJsonAsTemplate(this.value, query);
		return new JsonValueDataSetResource(json, getDataJsonPath());
	}

	/**
	 * JSON文本值数据集资源。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonValueDataSetResource extends JsonDataSetResource
	{
		private static final long serialVersionUID = 1L;

		public JsonValueDataSetResource()
		{
			super();
		}

		public JsonValueDataSetResource(String resolvedTemplate, String dataJsonPath)
		{
			super(resolvedTemplate, dataJsonPath);
		}

		@Override
		public Reader getReader() throws Throwable
		{
			return IOUtil.getReader(super.getResolvedTemplate());
		}

		@Override
		public boolean isIdempotent()
		{
			return true;
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
