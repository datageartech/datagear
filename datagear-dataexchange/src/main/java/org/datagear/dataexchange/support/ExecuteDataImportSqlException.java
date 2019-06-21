/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.SQLException;

import org.datagear.dataexchange.TextDataImportException;

/**
 * 执行数据导入SQL异常。
 * 
 * @author datagear@163.com
 *
 */
public class ExecuteDataImportSqlException extends TextDataImportException
{
	private static final long serialVersionUID = 1L;

	public ExecuteDataImportSqlException(int dataIndex, SQLException cause)
	{
		super(dataIndex, cause);
	}

	@Override
	public SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}
