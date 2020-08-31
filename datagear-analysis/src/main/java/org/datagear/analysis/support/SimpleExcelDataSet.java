/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;

/**
 * 简单Excel数据集。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleExcelDataSet extends AbstractExcelDataSet
{
	/** Excel文件 */
	private File file;

	public SimpleExcelDataSet()
	{
		super();
	}

	public SimpleExcelDataSet(String id, String name, File file)
	{
		super(id, name);
		this.file = file;
	}

	public SimpleExcelDataSet(String id, String name, List<DataSetProperty> properties, File file)
	{
		super(id, name, properties);
		this.file = file;
	}

	@Override
	protected File getExcelFile(Map<String, ?> paramValues) throws DataSetException
	{
		return this.file;
	}
}
