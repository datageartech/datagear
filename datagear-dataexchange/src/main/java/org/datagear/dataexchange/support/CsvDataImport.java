/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Reader;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.ExceptionResolve;
import org.datagear.dataexchange.TableTextDataImport;
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

	public CsvDataImport(ConnectionFactory connectionFactory, DataFormat dataFormat, boolean ignoreInexistentColumn,
			ExceptionResolve exceptionResolve, boolean nullForIllegalColumnValue, String table, Reader reader)
	{
		super(connectionFactory, dataFormat, ignoreInexistentColumn, exceptionResolve, nullForIllegalColumnValue,
				table);
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
