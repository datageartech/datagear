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
import org.datagear.analysis.DataSetProperty;
import org.datagear.util.FileUtil;

/**
 * 目录内CSV文件{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDirectoryFileDataSet extends AbstractCsvFileDataSet
{
	/** JSON文件所在的目录 */
	private File directory;

	/** JSON文件名 */
	private String fileName;

	public CsvDirectoryFileDataSet()
	{
		super();
	}

	public CsvDirectoryFileDataSet(String id, String name, File directory, String fileName)
	{
		super(id, name);
		this.directory = directory;
		this.fileName = fileName;
	}

	public CsvDirectoryFileDataSet(String id, String name, List<DataSetProperty> properties, File directory,
			String fileName)
	{
		super(id, name, properties);
		this.directory = directory;
		this.fileName = fileName;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	@Override
	protected File getCsvFile(Map<String, ?> paramValues) throws Throwable
	{
		File jsonFile = FileUtil.getFile(this.directory, this.fileName);
		return jsonFile;
	}
}
