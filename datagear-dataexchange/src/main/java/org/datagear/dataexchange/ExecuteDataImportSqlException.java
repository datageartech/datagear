/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.SQLException;

/**
 * 执行数据导入SQL异常。
 * 
 * @author datagear@163.com
 *
 */
public class ExecuteDataImportSqlException extends IndexDataExchangeException
{
	private static final long serialVersionUID = 1L;

	public ExecuteDataImportSqlException(DataIndex dataIndex, SQLException cause)
	{
		super(dataIndex, cause);
	}

	@Override
	public SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}
