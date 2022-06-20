/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import org.datagear.util.sqlvalidator.SqlValidation;

/**
 * SQL校验异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlValidationException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private final String sql;

	private final SqlValidation sqlValidation;

	public SqlValidationException(String sql, SqlValidation sqlValidation)
	{
		super(sqlValidation.getInvalidMessage());
		this.sql = sql;
		this.sqlValidation = sqlValidation;
	}

	public String getSql()
	{
		return sql;
	}

	public SqlValidation getSqlValidation()
	{
		return sqlValidation;
	}
}
