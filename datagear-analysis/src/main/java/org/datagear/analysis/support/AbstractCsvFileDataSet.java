/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;
import org.datagear.util.IOUtil;

/**
 * 抽象CSV文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvFileDataSet extends AbstractCsvDataSet
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	/** 文件编码 */
	private String encoding = DEFAULT_ENCODING;

	public AbstractCsvFileDataSet()
	{
		super();
	}

	public AbstractCsvFileDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractCsvFileDataSet(String id, String name, List<DataSetProperty> properties)
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
	protected Reader getCsvReader(Map<String, ?> paramValues) throws Throwable
	{
		File file = getCsvFile(paramValues);
		return IOUtil.getReader(file, this.encoding);
	}

	/**
	 * 获取CSV文件。
	 * <p>
	 * 实现方法应该返回实例级不变的文件。
	 * </p>
	 * 
	 * @param paramValues
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getCsvFile(Map<String, ?> paramValues) throws Throwable;
}
