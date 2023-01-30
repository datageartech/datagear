/*
 * Copyright 2018-2023 datagear.tech
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

import java.io.OutputStream;

import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.TextDataExportOption;
import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;

/**
 * Excel导出。
 * 
 * @author datagear@163.com
 *
 */
public class ExcelDataExport extends QueryTextDataExport
{
	private ResourceFactory<OutputStream> outputFactory;

	public ExcelDataExport()
	{
		super();
	}

	public ExcelDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataExportOption exportOption, Query query, ResourceFactory<OutputStream> outputFactory)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.outputFactory = outputFactory;
	}

	public ResourceFactory<OutputStream> getOutputFactory()
	{
		return outputFactory;
	}

	public void setOutputFactory(ResourceFactory<OutputStream> outputFactory)
	{
		this.outputFactory = outputFactory;
	}
}
