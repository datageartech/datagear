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
 * 目录内JSON文件{@linkplain DataSet}。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDirectoryFileDataSet extends AbstractJsonFileDataSet
{
	/** JSON文件所在的目录 */
	private File directory;

	/** JSON文件名 */
	private String fileName;

	public JsonDirectoryFileDataSet()
	{
		super();
	}

	public JsonDirectoryFileDataSet(String id, String name, File directory, String fileName)
	{
		super(id, name);
		this.directory = directory;
		this.fileName = fileName;
	}

	public JsonDirectoryFileDataSet(String id, String name, List<DataSetProperty> properties, File directory,
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
	protected File getJsonFile(Map<String, ?> paramValues) throws DataSetException
	{
		String fileName = resolveTemplate(this.fileName, paramValues);
		File jsonFile = FileUtil.getFile(directory, fileName);
		return jsonFile;
	}
}
