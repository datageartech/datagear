/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
