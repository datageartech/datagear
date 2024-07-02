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
import org.datagear.analysis.support.datasetres.CsvValueDataSetResource;

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
	private static final long serialVersionUID = 1L;

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

	public CsvValueDataSet(String id, String name, List<DataSetField> fields, String value)
	{
		super(id, name, fields);
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
	protected CsvValueDataSetResource getResource(DataSetQuery query) throws Throwable
	{
		String csv = resolveTemplateCsv(this.value, query);
		return new CsvValueDataSetResource(csv, getNameRow());
	}
}
