/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
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
	private Reader reader;

	/** 导入结果 */
	private TextDataImportResult importResult;

	public CsvDataImport()
	{
		super();
	}

	public CsvDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, TextDataImportOption importOption,
			String table, Reader reader)
	{
		super(connectionFactory, dataFormat, importOption, table);
		this.reader = reader;
	}

	public Reader getReader()
	{
		return reader;
	}

	public void setReader(Reader reader)
	{
		this.reader = reader;
	}

	public TextDataImportResult getImportResult()
	{
		return importResult;
	}

	public void setImportResult(TextDataImportResult importResult)
	{
		this.importResult = importResult;
	}
}
