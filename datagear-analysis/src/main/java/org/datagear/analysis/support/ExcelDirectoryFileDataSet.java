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

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.util.FileUtil;

/**
 * 目录内Excel文件{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDirectoryFileDataSet extends AbstractExcelDataSet
{
	/** Excel文件所在的目录 */
	private File directory;

	/** Excel文件名 */
	private String fileName;

	public ExcelDirectoryFileDataSet()
	{
		super();
	}

	public ExcelDirectoryFileDataSet(String id, String name, File directory, String fileName)
	{
		super(id, name);
		this.directory = directory;
		this.fileName = fileName;
	}

	/**
	 * @param id
	 * @param name
	 * @param properties
	 */
	public ExcelDirectoryFileDataSet(String id, String name, List<DataSetProperty> properties, File directory,
			String fileName)
	{
		super(id, name, properties);
		this.directory = directory;
		this.fileName = fileName;
	}

	@Override
	protected File getExcelFile(Map<String, ?> paramValues) throws DataSetException
	{
		File excelFile = FileUtil.getFile(this.directory, this.fileName);
		return excelFile;
	}
}
