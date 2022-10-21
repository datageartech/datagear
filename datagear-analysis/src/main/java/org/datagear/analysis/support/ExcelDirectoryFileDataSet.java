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
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetQuery;
import org.datagear.util.FileUtil;

/**
 * 目录内Excel文件{@linkplain DataSet}。
 * <p>
 * 注意：此类不支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDirectoryFileDataSet extends AbstractExcelFileDataSet
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
	protected File getExcelFile(DataSetQuery query) throws DataSetException
	{
		File excelFile = FileUtil.getFile(this.directory, this.fileName);
		return excelFile;
	}
}
