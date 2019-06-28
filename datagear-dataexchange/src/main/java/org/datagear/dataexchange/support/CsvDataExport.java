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
 * CSV导出。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataExport extends QueryTextDataExport
{
	private ResourceFactory<Writer> writerFactory;

	private TextDataExportListener listener;

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
	 * 构建{@linkplain CsvDataExport}列表。
	 * 
	 * @param connectionFactory
	 * @param dataFormat
	 * @param exportOption
	 * @param queries
	 * @param writerFactories
	 * @return
	 */
	public static List<CsvDataExport> valuesOf(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataExportOption exportOption, List<? extends Query> queries,
			List<? extends ResourceFactory<Writer>> writerFactories)
	{
		int size = queries.size();

		List<CsvDataExport> csvDataExports = new ArrayList<CsvDataExport>(size);

		for (int i = 0; i < size; i++)
		{
			CsvDataExport export = new CsvDataExport(connectionFactory, dataFormat, exportOption, queries.get(i),
					writerFactories.get(i));

			csvDataExports.add(export);
		}

		return csvDataExports;
	}
}
