/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
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
