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
public abstract class Export
{
	/** 导出源连接 */
	private Connection connection;

	/** 导出源结果集 */
	private ResultSet resultSet;

	/** 导出出错时是否终止 */
	private boolean abortOnError;

	private ExportReporter exportReporter;

	public Export()
	{
		super();
	}

	public Export(Connection connection, ResultSet resultSet, boolean abortOnError)
	{
		super();
		this.connection = connection;
		this.resultSet = resultSet;
		this.abortOnError = abortOnError;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public ResultSet getResultSet()
	{
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	public boolean isAbortOnError()
	{
		return abortOnError;
	}

	public void setAbortOnError(boolean abortOnError)
	{
		this.abortOnError = abortOnError;
	}

	public boolean hasExportReporter()
	{
		return (this.exportReporter != null);
	}

	public ExportReporter getExportReporter()
	{
		return exportReporter;
	}

	public void setExportReporter(ExportReporter exportReporter)
	{
		this.exportReporter = exportReporter;
	}
}
