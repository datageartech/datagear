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
public class CsvValueDataSet extends AbstractCsvDataSet
{
	/** CSV字符串 */
	private String value;

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
	protected TemplateResolvedSource<Reader> getCsvReader(DataSetQuery query) throws Throwable
	{
		String csv = resolveCsvAsTemplate(this.value, query);
		return new TemplateResolvedSource<>(IOUtil.getReader(csv), csv);
	}
}
