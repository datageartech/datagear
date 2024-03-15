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

import java.io.Reader;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.TableTextValueDataImport;
import org.datagear.dataexchange.ValueDataImportOption;
import org.datagear.util.StringUtil;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * JSON导入。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataImport extends TableTextValueDataImport
{
	private ResourceFactory<Reader> readerFactory;

	public JsonDataImport()
	{
		super();
	}

	public JsonDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, JsonDataImportOption importOption,
			ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory, dataFormat, importOption, null);
		this.readerFactory = readerFactory;
	}

	public JsonDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, JsonDataImportOption importOption,
			String table, ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory, dataFormat, importOption, table);
		this.readerFactory = readerFactory;
	}

	public ResourceFactory<Reader> getReaderFactory()
	{
		return readerFactory;
	}

	public void setReaderFactory(ResourceFactory<Reader> readerFactory)
	{
		this.readerFactory = readerFactory;
	}

	public boolean hasTable()
	{
		return !StringUtil.isEmpty(getTable());
	}

	@Override
	public JsonDataImportOption getImportOption()
	{
		return (JsonDataImportOption) super.getImportOption();
	}

	@Override
	public void setImportOption(ValueDataImportOption importOption)
	{
		if (!(importOption instanceof JsonDataImportOption))
			throw new IllegalArgumentException();

		super.setImportOption(importOption);
	}
}
