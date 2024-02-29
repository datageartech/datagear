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

package org.datagear.web.controller;

import org.datagear.connection.DriverEntity;

/**
 * 导入{@linkplain DriverEntity}文件格式非法异常。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalImportDriverEntityFileFormatException extends ControllerException
{
	private static final long serialVersionUID = 1L;

	public IllegalImportDriverEntityFileFormatException()
	{
		super();
	}

	public IllegalImportDriverEntityFileFormatException(String message)
	{
		super(message);
	}

	public IllegalImportDriverEntityFileFormatException(Throwable cause)
	{
		super(cause);
	}

	public IllegalImportDriverEntityFileFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
