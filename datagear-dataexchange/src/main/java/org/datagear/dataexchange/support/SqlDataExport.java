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

import java.io.Writer;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * SQL导出。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExport extends QueryTextDataExport
{
	private String tableName;
	private ResourceFactory<Writer> writerFactory;

	public SqlDataExport()
	{
		super();
	}

	public SqlDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, SqlDataExportOption exportOption,
			Query query, String tableName, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.tableName = tableName;
		this.writerFactory = writerFactory;
	}

	@Override
	public SqlDataExportOption getExportOption()
	{
		return (SqlDataExportOption) super.getExportOption();
	}

	@Override
	public void setExportOption(TextDataExportOption exportOption)
	{
		if (!(exportOption instanceof SqlDataExportOption))
			throw new IllegalArgumentException();

		super.setExportOption(exportOption);
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
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
