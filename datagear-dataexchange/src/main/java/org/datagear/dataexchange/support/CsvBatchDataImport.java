/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.ResourceFactory;
import org.datagear.dataexchange.TextBatchDataImport;
import org.datagear.dataexchange.TextDataImportOption;

/**
 * CSV批量导入。
 * 
 * @author datagear@163.com
 *
 */
public class CsvBatchDataImport extends TextBatchDataImport<CsvDataImport>
{
	private List<? extends ResourceFactory<Reader>> csvReaderFactories;

	private List<String> importTables;

	public CsvBatchDataImport()
	{
		super();
	}

	public CsvBatchDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataImportOption importOption, List<? extends ResourceFactory<Reader>> csvReaderFactories,
			List<String> importTables)
	{
		super(connectionFactory, dataFormat, importOption);
		this.csvReaderFactories = csvReaderFactories;
		this.importTables = importTables;
	}

	public List<? extends ResourceFactory<Reader>> getCsvReaderFactories()
	{
		return csvReaderFactories;
	}

	public void setCsvReaderFactories(List<? extends ResourceFactory<Reader>> csvReaderFactories)
	{
		this.csvReaderFactories = csvReaderFactories;
	}

	public List<String> getImportTables()
	{
		return importTables;
	}

	public void setImportTables(List<String> importTables)
	{
		this.importTables = importTables;
	}

	@Override
	public List<CsvDataImport> split() throws DataExchangeException
	{
		List<? extends ResourceFactory<Reader>> csvReaderFactories = getCsvReaderFactories();
		int csvReaderFactoriesSize = csvReaderFactories.size();
		List<String> importTables = getImportTables();
		List<CsvDataImport> csvDataImports = new ArrayList<CsvDataImport>(csvReaderFactoriesSize);

		for (int i = 0; i < csvReaderFactoriesSize; i++)
		{
			CsvDataImport csvDataImport = new CsvDataImport(getConnectionFactory(), getDataFormat(), getImportOption(),
					importTables.get(i), csvReaderFactories.get(i));

			csvDataImports.add(csvDataImport);
		}

		return csvDataImports;
	}
}
