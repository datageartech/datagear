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

import org.datagear.dataexchange.DataExchange;
import org.datagear.dataexchange.DataImportListener;
import org.datagear.dataexchange.DataImportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * SQL数据导入。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataImport extends DataExchange
{
	private DataImportOption importOption;

	private ResourceFactory<Reader> readerFactory;

	private DataImportListener listener;

	public SqlDataImport()
	{
		super();
	}

	public SqlDataImport(ConnectionFactory connectionFactory, DataImportOption importOption,
			ResourceFactory<Reader> readerFactory)
	{
		super(connectionFactory);
		this.importOption = importOption;
		this.readerFactory = readerFactory;
	}

	public DataImportOption getImportOption()
	{
		return importOption;
	}

	public void setImportOption(DataImportOption importOption)
	{
		this.importOption = importOption;
	}

	public ResourceFactory<Reader> getReaderFactory()
	{
		return readerFactory;
	}

	public void setReaderFactory(ResourceFactory<Reader> readerFactory)
	{
		this.readerFactory = readerFactory;
	}

	@Override
	public DataImportListener getListener()
	{
		return listener;
	}

	public void setListener(DataImportListener listener)
	{
		this.listener = listener;
	}
}
