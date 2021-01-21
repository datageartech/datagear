/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
 * JSON导出。
 * 
 * @author datagear@163.com
 *
 */
public class JsonDataExport extends QueryTextDataExport
{
	private ResourceFactory<Writer> writerFactory;

	/**
	 * 当{@linkplain JsonDataExportOption#getJsonDataFormat()}为{@linkplain JsonDataFormat#TABLE_OBJECT}时，设置导出表名。
	 */
	private String tableName;

	public JsonDataExport()
	{
		super();
	}

	public JsonDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, JsonDataExportOption exportOption,
			Query query, ResourceFactory<Writer> writerFactory, String tableName)
	{
		super(connectionFactory, dataFormat, exportOption, query);
		this.tableName = tableName;
		this.writerFactory = writerFactory;
	}

	@Override
	public JsonDataExportOption getExportOption()
	{
		return (JsonDataExportOption) super.getExportOption();
	}

	@Override
	public void setExportOption(TextDataExportOption exportOption)
	{
		if (!(exportOption instanceof JsonDataExportOption))
			throw new IllegalArgumentException();

		super.setExportOption(exportOption);
	}

	public ResourceFactory<Writer> getWriterFactory()
	{
		return writerFactory;
	}

	public void setWriterFactory(ResourceFactory<Writer> writerFactory)
	{
		this.writerFactory = writerFactory;
	}

	public boolean hasTableName()
	{
		return (this.tableName != null && !this.tableName.isEmpty());
	}

	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}
}
