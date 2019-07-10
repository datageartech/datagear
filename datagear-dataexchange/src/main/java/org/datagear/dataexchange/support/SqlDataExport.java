/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.Query;
import org.datagear.dataexchange.QueryTextDataExport;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TextDataExportListener;
import org.datagear.dataexchange.TextDataExportOption;

/**
 * SQL导出。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataExport extends QueryTextDataExport
{
	private TextDataExportOption exportOption;
	private String tableName;
	private ResourceFactory<Writer> writerFactory;
	private TextDataExportListener listener;

	public SqlDataExport()
	{
		super();
	}

	public SqlDataExport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataExportOption exportOption,
			Query query, String tableName, ResourceFactory<Writer> writerFactory)
	{
		super(connectionFactory, dataFormat, query);
		this.exportOption = exportOption;
		this.tableName = tableName;
		this.writerFactory = writerFactory;
	}

	@Override
	public TextDataExportOption getExportOption()
	{
		return exportOption;
	}

	public void setExportOption(TextDataExportOption exportOption)
	{
		this.exportOption = exportOption;
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

	@Override
	public TextDataExportListener getListener()
	{
		return listener;
	}

	public void setListener(TextDataExportListener listener)
	{
		this.listener = listener;
	}

	/**
	 * 构建{@linkplain SqlDataExport}列表。
	 * 
	 * @param connectionFactory
	 * @param dataFormat
	 * @param exportOption
	 * @param queries
	 * @param tableNames
	 * @param writerFactories
	 * @return
	 */
	public static List<SqlDataExport> valuesOf(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataExportOption exportOption, List<? extends Query> queries, List<String> tableNames,
			List<? extends ResourceFactory<Writer>> writerFactories)
	{
		int size = queries.size();

		List<SqlDataExport> dataExports = new ArrayList<SqlDataExport>(size);

		for (int i = 0; i < size; i++)
		{
			SqlDataExport export = new SqlDataExport(connectionFactory, dataFormat, exportOption, queries.get(i),
					tableNames.get(i), writerFactories.get(i));

			dataExports.add(export);
		}

		return dataExports;
	}
}
