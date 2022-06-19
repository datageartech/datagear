/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;

import org.datagear.util.JdbcSupport;
import org.datagear.util.QueryResultSet;
import org.datagear.util.Sql;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.util.sqlvalidator.SqlValidator;

/**
 * 抽象{@linkplain Query}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractQuery extends JdbcSupport implements Query
{
	private SqlValidator sqlValidator;

	public AbstractQuery()
	{
		super();
	}

	public SqlValidator getSqlValidator()
	{
		return sqlValidator;
	}

	public void setSqlValidator(SqlValidator sqlValidator)
	{
		this.sqlValidator = sqlValidator;
	}

	/**
	 * 执行查询，当{@linkplain #getSqlValidator()}不为{@code null}时，在查询前进行SQL校验。
	 * 
	 * @param cn
	 * @param sql
	 * @return
	 * @throws SqlValidationException
	 * @throws Throwable
	 */
	protected QueryResultSet executeQueryValidation(Connection cn, String sql) throws SqlValidationException, Throwable
	{
		if (this.sqlValidator != null)
		{
			SqlValidation validation = this.sqlValidator.validate(sql, DatabaseProfile.valueOf(cn));
			if (!validation.isValid())
				throw new SqlValidationException(sql, validation);
		}

		return executeQuery(cn, Sql.valueOf(sql), ResultSet.TYPE_FORWARD_ONLY);
	}
}
