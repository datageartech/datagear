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
import java.util.List;

import org.datagear.analysis.DataSetField;
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

	public SimpleCsvFileDataSet(String id, String name, List<DataSetField> fields, File file)
	{
		super(id, name, fields);
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
