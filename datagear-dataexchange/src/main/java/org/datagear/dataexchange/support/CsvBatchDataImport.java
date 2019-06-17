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
import org.datagear.dataexchange.TextBatchDataImport;
import org.datagear.dataexchange.TextDataImportOption;

/**
 * CSV批量导入。
 * <p>
 * CSV批量导入服务类在导入完成后，应该关闭{@linkplain #getCsvReaders()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CsvBatchDataImport extends TextBatchDataImport<CsvDataImport>
{
	private List<Reader> csvReaders;

	private List<String> importTables;

	public CsvBatchDataImport()
	{
		super();
	}

	public CsvBatchDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat,
			TextDataImportOption importOption, List<Reader> csvReaders, List<String> importTables)
	{
		super(connectionFactory, dataFormat, importOption);
		this.csvReaders = csvReaders;
		this.importTables = importTables;
	}

	public List<Reader> getCsvReaders()
	{
		return csvReaders;
	}

	public void setCsvReaders(List<Reader> csvReaders)
	{
		this.csvReaders = csvReaders;
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
		List<Reader> csvReaders = getCsvReaders();
		List<String> importTables = getImportTables();
		List<CsvDataImport> csvDataImports = new ArrayList<CsvDataImport>(csvReaders.size());

		for (int i = 0; i < csvReaders.size(); i++)
		{
			CsvDataImport csvDataImport = new CsvDataImport(getConnectionFactory(), getDataFormat(), getImportOption(),
					importTables.get(i), csvReaders.get(i));

			csvDataImports.add(csvDataImport);
		}

		return csvDataImports;
	}
}
