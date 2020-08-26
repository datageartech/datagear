/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvableDataSet;
import org.datagear.util.FileUtil;

/**
 * 抽象Excel数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractExcelDataSet extends AbstractFmkTemplateDataSet implements ResolvableDataSet
{
	public static final String EXTENSION_XLSX = "xlsx";

	public static final String EXTENSION_XLS = "xls";

	public AbstractExcelDataSet()
	{
	}

	@SuppressWarnings("unchecked")
	public AbstractExcelDataSet(String id, String name)
	{
		super(id, name, Collections.EMPTY_LIST);
	}

	public AbstractExcelDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		// TODO
		return null;
	}

	/**
	 * 获取Excel文件。
	 * 
	 * @param paramValues
	 * @return
	 * @throws DataSetException
	 */
	protected abstract File getExcelFile(Map<String, ?> paramValues) throws DataSetException;

	/**
	 * 给定Excel文件是否是老版本的{@code .xls}文件。
	 * 
	 * @param file
	 * @return
	 */
	protected boolean isXls(File file)
	{
		return FileUtil.isExtension(file, EXTENSION_XLS);
	}
}
