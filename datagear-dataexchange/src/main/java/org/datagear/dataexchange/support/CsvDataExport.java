/*
 * Copyright 2018-2024 datagear.tech
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

import java.io.Writer;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * CSV导出。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExport extends QueryTextDataExport
{
	private ResourceFactory<Writer> writerFactory;

	public CsvDataExport()
	{
		super();
	}

	public CsvDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption,
			Query query, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.writerFactory = writerFactory;
	}

	public ResourceFactory<Writer> getWriterFactory()
	{
		return writerFactory;
	}

	public void setWriterFactory(ResourceFactory<Writer> writerFactory)
	{
		this.writerFactory = writerFactory;
	}
}
