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

	public Export()
	{
		super();
	}

	public Export(Connection connection, ResultSet resultSet)
	{
		super();
		this.connection = connection;
		this.resultSet = resultSet;
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
}
