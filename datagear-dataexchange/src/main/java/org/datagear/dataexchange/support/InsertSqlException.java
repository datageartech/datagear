/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.SQLException;

import org.datagear.dataexchange.DataImportException;

/**
 * 导入时执行插入SQL异常。
 * 
 * @author datagear@163.com
 *
 */
public class InsertSqlException extends DataImportException
{
	private static final long serialVersionUID = 1L;

	private String table;

	private int dataIndex;

	public InsertSqlException(String table, int dataIndex, SQLException e)
	{
		super(e);
		this.table = table;
		this.dataIndex = dataIndex;
	}

	public String getTable()
	{
		return table;
	}

	protected void setTable(String table)
	{
		this.table = table;
	}

	public int getDataIndex()
	{
		return dataIndex;
	}

	protected void setDataIndex(int dataIndex)
	{
		this.dataIndex = dataIndex;
	}

	public SQLException getSQLException()
	{
		return (SQLException) getCause();
	}
}
