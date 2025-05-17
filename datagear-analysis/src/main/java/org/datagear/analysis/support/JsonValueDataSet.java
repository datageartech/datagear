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

package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.datasetres.JsonValueDataSetResource;

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
	private static final long serialVersionUID = 1L;

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

	public JsonValueDataSet(String id, String name, List<DataSetField> fields, String value)
	{
		super(id, name, fields);
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
	protected JsonValueDataSetResource getResource(DataSetQuery query) throws Throwable
	{
		String json = resolveTemplateJson(this.value, query);
		return new JsonValueDataSetResource(json, getDataJsonPath(), getAdditionDataProps());
	}
}
