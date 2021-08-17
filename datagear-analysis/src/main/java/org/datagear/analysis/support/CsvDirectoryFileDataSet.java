/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.util.FileUtil;

/**
 * 目录内CSV文件{@linkplain DataSet}。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CsvDirectoryFileDataSet extends AbstractCsvFileDataSet
{
	private static final long serialVersionUID = 1L;

	/** CSV文件所在的目录 */
	private File directory;

	/** CSV文件名 */
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
	protected File getCsvFile(DataSetQuery query) throws Throwable
	{
		File jsonFile = FileUtil.getFile(this.directory, this.fileName);
		return jsonFile;
	}
}
