/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.util.IOUtil;

/**
 * CSV值数据集。
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
	protected Reader getCsvReader(Map<String, ?> paramValues) throws Throwable
	{
		return IOUtil.getReader(this.value);
	}
}
