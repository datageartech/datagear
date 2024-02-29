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

package org.datagear.web.sqlpad;

import org.datagear.management.domain.Schema;
import org.datagear.management.domain.User;
import org.datagear.util.SqlScriptParser.SqlStatement;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.util.sqlvalidator.SqlValidator;

/**
 * SQL执行权限校验器。
 * 
 * @author datagear@163.com
 *
 */
public class SqlPermissionValidator
{
	private SqlValidator readPermissionSqlValidator;

	private SqlValidator editPermissionSqlValidator;

	private SqlValidator deletePermissionSqlValidator;

	public SqlPermissionValidator(SqlValidator readPermissionSqlValidator, SqlValidator editPermissionSqlValidator,
			SqlValidator deletePermissionSqlValidator)
	{
		super();
		this.readPermissionSqlValidator = readPermissionSqlValidator;
		this.editPermissionSqlValidator = editPermissionSqlValidator;
		this.deletePermissionSqlValidator = deletePermissionSqlValidator;
	}

	public SqlValidator getReadPermissionSqlValidator()
	{
		return readPermissionSqlValidator;
	}

	public void setReadPermissionSqlValidator(SqlValidator readPermissionSqlValidator)
	{
		this.readPermissionSqlValidator = readPermissionSqlValidator;
	}

	public SqlValidator getEditPermissionSqlValidator()
	{
		return editPermissionSqlValidator;
	}

	public void setEditPermissionSqlValidator(SqlValidator editPermissionSqlValidator)
	{
		this.editPermissionSqlValidator = editPermissionSqlValidator;
	}

	public SqlValidator getDeletePermissionSqlValidator()
	{
		return deletePermissionSqlValidator;
	}

	public void setDeletePermissionSqlValidator(SqlValidator deletePermissionSqlValidator)
	{
		this.deletePermissionSqlValidator = deletePermissionSqlValidator;
	}

	/**
	 * 检查用户是否有执行指定SQL的权限。
	 * 
	 * @param user
	 * @param schema
	 * @param sqlStatement
	 * @param databaseProfile
	 * @return
	 */
	public SqlValidation validate(User user, Schema schema, SqlStatement sqlStatement, DatabaseProfile databaseProfile)
	{
		String sql = sqlStatement.getSql();
		int permission = schema.getDataPermission();

		if (Schema.isDeleteTableDataPermission(permission))
		{
			return this.deletePermissionSqlValidator.validate(sql, databaseProfile);
		}
		else if (Schema.isEditTableDataPermission(permission))
		{
			return this.editPermissionSqlValidator.validate(sql, databaseProfile);
		}
		else if (Schema.isReadTableDataPermission(permission))
		{
			return this.readPermissionSqlValidator.validate(sql, databaseProfile);
		}

		return new SqlValidation("ANY KEYWORD");
	}
}
