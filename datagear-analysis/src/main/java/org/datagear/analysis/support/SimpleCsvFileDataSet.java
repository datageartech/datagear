/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetProperty;

/**
 * 简单CSV文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleCsvFileDataSet extends AbstractCsvFileDataSet
{
	/** CSV文件 */
	private File file;

	public SimpleCsvFileDataSet()
	{
		super();
	}

	public SimpleCsvFileDataSet(String id, String name, File file)
	{
		super(id, name);
		this.file = file;
	}

	public SimpleCsvFileDataSet(String id, String name, List<DataSetProperty> properties, File file)
	{
		super(id, name, properties);
		this.file = file;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	@Override
	protected File getCsvFile(Map<String, ?> paramValues) throws Throwable
	{
		return this.file;
	}
}
