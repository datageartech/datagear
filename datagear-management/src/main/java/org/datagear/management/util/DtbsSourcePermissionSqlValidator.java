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
 * 数据源权限{@linkplain SqlValidator}。
 * 
 * @author datagear@163.com
 *
 */
public class DtbsSourcePermissionSqlValidator implements SqlValidator
{
	private DtbsSourceSqlPermissionValidator dtbsSourceSqlPermissionValidator;

	/**
	 * 数据源权限，参考{@linkplain DtbsSource#getDataPermission()}
	 */
	private int permission;

	public DtbsSourcePermissionSqlValidator()
	{
		super();
	}

	public DtbsSourcePermissionSqlValidator(DtbsSourceSqlPermissionValidator dtbsSourceSqlPermissionValidator,
			int permission)
	{
		super();
		this.dtbsSourceSqlPermissionValidator = dtbsSourceSqlPermissionValidator;
		this.permission = permission;
	}

	public DtbsSourceSqlPermissionValidator getDtbsSourceSqlPermissionValidator()
	{
		return dtbsSourceSqlPermissionValidator;
	}

	public void setDtbsSourceSqlPermissionValidator(DtbsSourceSqlPermissionValidator dtbsSourceSqlPermissionValidator)
	{
		this.dtbsSourceSqlPermissionValidator = dtbsSourceSqlPermissionValidator;
	}

	public int getPermission()
	{
		return permission;
	}

	public void setPermission(int permission)
	{
		this.permission = permission;
	}

	@Override
	public SqlValidation validate(String sql, DatabaseProfile profile)
	{
		return this.dtbsSourceSqlPermissionValidator.validate(this.permission, sql, profile);
	}
}
