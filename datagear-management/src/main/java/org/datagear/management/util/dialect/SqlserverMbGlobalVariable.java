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

package org.datagear.management.util.dialect;

/**
 * SQL Server Mybatis全局变量。
 * 
 * @author datagear@163.com
 *
 */
public class SqlserverMbGlobalVariable extends MbGlobalVariable
{
	public static final String DEFAULT_FUNC_PREFIX = "dbo.";

	private String funcPrefix = DEFAULT_FUNC_PREFIX;

	public SqlserverMbGlobalVariable()
	{
		super();
	}

	public String getFuncPrefix()
	{
		return funcPrefix;
	}

	public void setFuncPrefix(String funcPrefix)
	{
		this.funcPrefix = funcPrefix;
	}

	@Override
	public String funcModInt()
	{
		return getFuncPrefix() + super.funcModInt();
	}
}
