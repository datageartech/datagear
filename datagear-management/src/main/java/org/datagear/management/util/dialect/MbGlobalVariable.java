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

import java.util.Properties;

/**
 * Mybatis全局变量。
 * 
 * @author datagear@163.com
 *
 */
public class MbGlobalVariable
{
	/**
	 * 变量名：MAX函数
	 */
	public static final String VAR_FUNC_MAX = "FUNC_MAX";

	/**
	 * 变量名：整数取余函数
	 */
	public static final String VAR_FUNC_MODINT = "FUNC_MODINT";

	public MbGlobalVariable()
	{
		super();
	}

	/**
	 * 填充全局变量。
	 * 
	 * @param variables
	 */
	public void inflate(Properties variables)
	{
		variables.put(VAR_FUNC_MAX, funcMax());
		variables.put(VAR_FUNC_MODINT, funcModInt());
	}

	/**
	 * MAX函数名。
	 * 
	 * @return
	 */
	public String funcMax()
	{
		return "MAX";
	}

	/**
	 * 求余函数名。
	 * 
	 * @return
	 */
	public String funcModInt()
	{
		return "DATAGEAR_FUNC_MODINT";
	}
}
