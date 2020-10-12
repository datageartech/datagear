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
 * 抽象JSON文件数据集。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonFileDataSet extends AbstractJsonDataSet
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
	protected TemplateResolvedSource<Reader> getJsonReader(Map<String, ?> paramValues) throws Throwable
	{
		File file = getJsonFile(paramValues);
		return new TemplateResolvedSource<>(IOUtil.getReader(file, this.encoding));
	}

	/**
	 * 获取JSON文件。
	 * <p>
	 * 实现方法应该返回实例级不变的文件。
	 * </p>
	 * 
	 * @param paramValues
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getJsonFile(Map<String, ?> paramValues) throws Throwable;
}
