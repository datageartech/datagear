/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.support.datasetres.ExcelFileDataSetResource;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 抽象Excel文件数据集。
 * <p>
 * 此类的{@linkplain #getSheetName()}支持<code>Freemarker</code>模板语言。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractExcelFileDataSet<T extends ExcelFileDataSetResource> extends AbstractExcelDataSet<T>
{
	private static final long serialVersionUID = 1L;

	public AbstractExcelFileDataSet()
	{
		super();
	}

	public AbstractExcelFileDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractExcelFileDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
	}

	@Override
	protected InputStream getResourceInputStream(T resource) throws Throwable
	{
		File file = FileUtil.getFile(resource.getAbsolutePath());
		return IOUtil.getInputStream(file);
	}

	/**
	 * 创建{@linkplain ExcelFileDataSetResource}实例。
	 * 
	 * @param resolvedTemplate
	 * @param file
	 * @param sheetName
	 * @return
	 */
	protected ExcelFileDataSetResource toExcelFileDataSetResource(String resolvedTemplate, File file, String sheetName)
	{
		ExcelFileDataSetResource res = new ExcelFileDataSetResource(resolvedTemplate, sheetName, getSheetIndex(),
				getNameRow(), getDataRowExp(), getDataColumnExp(), (isForceXls() ? true : isXls(file.getName())),
				file.getAbsolutePath(), file.lastModified());
		return res;
	}

	protected String resolveTemplateSheetName(DataSetQuery query)
	{
		String sheetName = getSheetName();
		
		if(!StringUtil.isEmpty(sheetName))
			sheetName = resolveTemplateSheetName(sheetName, query);
		
		return sheetName;
	}

	/**
	 * 解析模板：工作表名。
	 * <p>
	 * 实现规则应与{@linkplain AbstractDataSet#resolveTemplatePlain(String, DataSetQuery)}一致。
	 * </p>
	 * 
	 * @param fileName
	 * @param query
	 * @return
	 */
	protected String resolveTemplateSheetName(String sheetName, DataSetQuery query)
	{
		return resolveTemplatePlain(sheetName, query);
	}

	/**
	 * 给定Excel文件是否是老版本的{@code .xls}文件。
	 * 
	 * @param fileName
	 * @return
	 */
	protected boolean isXls(String fileName)
	{
		return FileUtil.isExtension(fileName, EXTENSION_XLS);
	}
}
