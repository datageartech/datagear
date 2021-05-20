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
import org.datagear.util.IOUtil;

/**
 * 抽象CSV文件数据集。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvFileDataSet extends AbstractCsvDataSet
{
	/** 文件编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;

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
	protected TemplateResolvedSource<Reader> getCsvReader(DataSetQuery query) throws Throwable
	{
		File file = getCsvFile(query);
		return new TemplateResolvedSource<>(IOUtil.getReader(file, this.encoding));
	}

	/**
	 * 获取CSV文件。
	 * <p>
	 * 实现方法应该返回实例级不变的文件。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getCsvFile(DataSetQuery query) throws Throwable;
}
