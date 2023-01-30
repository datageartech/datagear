/*
 * Copyright 2018-2023 datagear.tech
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

import org.datagear.management.domain.DataSetResDirectory;

/**
 * {@linkplain DataSetResDirectory#getDirectory()}未找到异常。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResDirectoryNotFoundException extends IllegalInputException
{
	private static final long serialVersionUID = 1L;

	private String directory;

	public DataSetResDirectoryNotFoundException(String directory)
	{
		super();
		this.directory = directory;
	}

	public DataSetResDirectoryNotFoundException(String directory, String message)
	{
		super(message);
		this.directory = directory;
	}

	public DataSetResDirectoryNotFoundException(String directory, Throwable cause)
	{
		super(cause);
		this.directory = directory;
	}

	public DataSetResDirectoryNotFoundException(String directory, String message, Throwable cause)
	{
		super(message, cause);
		this.directory = directory;
	}

	public String getDirectory()
	{
		return directory;
	}

	protected void setDirectory(String directory)
	{
		this.directory = directory;
	}

}
