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

package org.datagear.dataexchange.support;

import java.io.File;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.TextValueDataImport;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.resource.ConnectionFactory;

/**
 * Excel导入。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataImport extends TextValueDataImport
{
	private File file;

	private boolean xls = false;

	private String unifiedTable;

	public ExcelDataImport()
	{
		super();
	}

	public ExcelDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			ValueDataImportOption importOption, File file)
	{
		super(connectionFactory, dataFormat, importOption);
		this.file = file;

		String fileName = this.file.getName().toLowerCase();
		this.xls = fileName.endsWith(".xls");
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	/**
	 * 是否是{@code .xls}文件。
	 * 
	 * @return {@code true}，是{@code .xls}文件；{@code false} 是{@code .xlsx}文件。
	 */
	public boolean isXls()
	{
		return xls;
	}

	public void setXls(boolean xls)
	{
		this.xls = xls;
	}

	/**
	 * 是否设置了统一表名称。
	 * <p>
	 * 返回{@code true}，表示所有工作表导入同一表中；返回{@code false}，表示各自工作表导入其名称对应的表中。
	 * </p>
	 * 
	 * @return
	 */
	public boolean hasUnifiedTable()
	{
		return (this.unifiedTable != null && !this.unifiedTable.isEmpty());
	}

	public String getUnifiedTable()
	{
		return unifiedTable;
	}

	public void setUnifiedTable(String unifiedTable)
	{
		this.unifiedTable = unifiedTable;
	}
}
