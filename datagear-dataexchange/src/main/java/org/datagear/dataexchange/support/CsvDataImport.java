/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TableTextDataImport;
import org.datagear.dataexchange.TextDataImportOption;
import org.datagear.dataexchange.TextDataImportResult;

/**
 * CSV导入。
 * 
 * @author datagear@163.com
 *
 */
public class CsvDataImport extends TableTextDataImport
{
	/** CSV输入流 */
	private ResourceFactory<Reader> readerFactory;

	/** 导入结果 */
	private TextDataImportResult importResult;

	public CsvDataImport()
	{
		super();
	}

	public CsvDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataImportOption importOption,
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

	public TextDataImportResult getImportResult()
	{
		return importResult;
	}

	public void setImportResult(TextDataImportResult importResult)
	{
		this.importResult = importResult;
	}

	/**
	 * 构建{@linkplain CsvDataImport}列表。
	 * 
	 * @param connectionFactory
	 * @param dataFormat
	 * @param importOption
	 * @param tables
	 * @param readerFactories
	 * @return
	 */
	public static List<CsvDataImport> valuesOf(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataImportOption importOption, List<String> tables,
			List<? extends ResourceFactory<Reader>> readerFactories)
	{
		List<CsvDataImport> csvDataImports = new ArrayList<CsvDataImport>(tables.size());

		for (int i = 0; i < tables.size(); i++)
		{
			CsvDataImport csvDataImport = new CsvDataImport(connectionFactory, dataFormat, importOption, tables.get(i),
					readerFactories.get(i));

			csvDataImports.add(csvDataImport);
		}

		return csvDataImports;
	}
}
