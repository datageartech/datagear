/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * 导出端。
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataExport extends DataExchange
{
	/** 导出源结果集 */
	private ResultSet resultSet;

	private DataExportReporter dataExportReporter;

	public DataExport()
	{
		super();
	}

	public DataExport(Connection connection, boolean abortOnError, ResultSet resultSet)
	{
		super(connection, abortOnError);
		this.resultSet = resultSet;
	}

	public ResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	public boolean hasDataExportReporter()
	{
		return (this.dataExportReporter != null);
	}

	public DataExportReporter getDataExportReporter()
	{
		return dataExportReporter;
	}

	public void setDataExportReporter(DataExportReporter dataExportReporter)
	{
		this.dataExportReporter = dataExportReporter;
	}
}
