/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.util;

import org.datagear.management.domain.DtbsSource;
import org.datagear.util.sqlvalidator.DatabaseProfile;
import org.datagear.util.sqlvalidator.SqlValidation;
import org.datagear.util.sqlvalidator.SqlValidator;

/**
 * 数据源SQL执行权限校验器。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourceSqlPermissionValidator
{
	private SqlValidator readPermissionSqlValidator;

	private SqlValidator editPermissionSqlValidator;

	private SqlValidator deletePermissionSqlValidator;

	public DtbsSourceSqlPermissionValidator(SqlValidator readPermissionSqlValidator, SqlValidator editPermissionSqlValidator,
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
	 * 检查对{@linkplain DtbsSource}有指定权限时，是否有执行指定SQL的权限。
	 * 
	 * @param permission
	 *            数据源权限，参考{@linkplain DtbsSource#getDataPermission()}
	 * @param sql
	 * @param databaseProfile
	 * @return
	 */
	public SqlValidation validate(int permission, String sql, DatabaseProfile databaseProfile)
	{
		if (DtbsSource.isDeleteTableDataPermission(permission))
		{
			return this.deletePermissionSqlValidator.validate(sql, databaseProfile);
		}
		else if (DtbsSource.isEditTableDataPermission(permission))
		{
			return this.editPermissionSqlValidator.validate(sql, databaseProfile);
		}
		else if (DtbsSource.isReadTableDataPermission(permission))
		{
			return this.readPermissionSqlValidator.validate(sql, databaseProfile);
		}
		else
		{
			return new SqlValidation("ALL");
		}
	}
}
