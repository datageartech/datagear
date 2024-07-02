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
import org.datagear.analysis.support.datasetres.CsvFileDataSetResource;
import org.datagear.util.IOUtil;

/**
 * 抽象CSV文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractCsvFileDataSet extends AbstractCsvDataSet<CsvFileDataSetResource>
{
	private static final long serialVersionUID = 1L;

	/** 文件编码 */
	private String encoding = IOUtil.CHARSET_UTF_8;

	public AbstractCsvFileDataSet()
	{
		super();
	}

	public AbstractCsvFileDataSet(String id, String name)
	{
		super(id, name);
	}

	public AbstractCsvFileDataSet(String id, String name, List<DataSetField> fields)
	{
		super(id, name, fields);
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	protected CsvFileDataSetResource getResource(DataSetQuery query)
			throws Throwable
	{
		File file = getCsvFile(query);
		return new CsvFileDataSetResource("", getNameRow(), getEncoding(), file.getAbsolutePath(), file.lastModified());
	}

	/**
	 * 获取CSV文件。
	 * 
	 * @param query
	 * @return
	 * @throws Throwable
	 */
	protected abstract File getCsvFile(DataSetQuery query) throws Throwable;
}
