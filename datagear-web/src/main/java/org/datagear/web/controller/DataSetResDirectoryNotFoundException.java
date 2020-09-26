/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
