/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import org.datagear.analysis.DataSetException;
import org.datagear.util.sqlvalidator.SqlValidation;

/**
 * {@linkplain SqlDataSet}的SQL校验异常。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDataSetSqlValidationException extends DataSetException
{
	private static final long serialVersionUID = 1L;

	private final String sql;

	private final SqlValidation sqlValidation;

	public SqlDataSetSqlValidationException(String sql, SqlValidation sqlValidation)
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
