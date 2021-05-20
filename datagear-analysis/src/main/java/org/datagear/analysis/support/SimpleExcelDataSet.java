/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.util.List;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;

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
	protected File getExcelFile(DataSetQuery query) throws DataSetException
	{
		return this.file;
	}
}
