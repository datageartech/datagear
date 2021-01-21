/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
