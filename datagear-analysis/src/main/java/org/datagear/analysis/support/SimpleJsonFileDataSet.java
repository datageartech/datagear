/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;

/**
 * 简单JSON文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleJsonFileDataSet extends AbstractJsonFileDataSet
{
	/** JSON文件 */
	private File file;

	public SimpleJsonFileDataSet()
	{
		super();
	}

	public SimpleJsonFileDataSet(String id, String name, File file)
	{
		super(id, name);
		this.file = file;
	}

	public SimpleJsonFileDataSet(String id, String name, List<DataSetProperty> properties, File file)
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
	protected File getJsonFile(Map<String, ?> paramValues) throws DataSetException
	{
		return this.file;
	}
}
