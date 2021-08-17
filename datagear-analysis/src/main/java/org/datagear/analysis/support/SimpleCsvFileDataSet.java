/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;

/**
 * 简单CSV文件数据集。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimpleCsvFileDataSet extends AbstractCsvFileDataSet
{
	private static final long serialVersionUID = 1L;

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
	protected File getCsvFile(DataSetQuery query) throws Throwable
	{
		return this.file;
	}
}
