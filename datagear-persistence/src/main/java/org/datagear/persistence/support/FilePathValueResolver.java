/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 文件路径列值处理器。
 * <p>
 * 此类用于为{@linkplain ConversionSqlParamValueMapper}支持获取特定格式的文件路径字符串所表示的文件及其输入流。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FilePathValueResolver
{
	/** 文件路径值前缀 */
	public static final String FILE_PATH_VALUE_PREFIX = "file:";

	/** 文件的字符集 */
	private String fileCharset;

	public FilePathValueResolver()
	{
		super();
	}

	public boolean hasFileCharset()
	{
		return !StringUtil.isEmpty(this.fileCharset);
	}

	public String getFileCharset()
	{
		return fileCharset;
	}

	public void setFileCharset(String fileCharset)
	{
		this.fileCharset = fileCharset;
	}

	/**
	 * 获取指定文件路径值的文件。
	 * 
	 * @param table
	 * @param column
	 * @param filePathValue
	 * @return 返回{@code null}表示不是文件路径值
	 * @throws SqlParamValueMapperException
	 */
	public File getFileValue(Table table, Column column, String filePathValue) throws SqlParamValueMapperException
	{
		if (!isFilePathValue(filePathValue))
			return null;

		File file = new File(getFilePath(filePathValue));

		return (file.exists() ? file : null);
	}

	/**
	 * 获取文件值输入流。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	public InputStream getInputStream(Table table, Column column, File value) throws SqlParamValueMapperException
	{
		try
		{
			return IOUtil.getInputStream(value);
		}
		catch (FileNotFoundException e)
		{
			throw new SqlParamValueMapperException(e);
		}
	}

	/**
	 * 获取文件值输入流。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	public Reader getReader(Table table, Column column, File value) throws SqlParamValueMapperException
	{
		try
		{
			return IOUtil.getReader(value, this.fileCharset);
		}
		catch (IOException e)
		{
			throw new SqlParamValueMapperException(e);
		}
	}

	/**
	 * 给定值是否是文件路径值。
	 * 
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	public boolean isFilePathValue(String value) throws SqlParamValueMapperException
	{
		return (value != null && value.startsWith(FILE_PATH_VALUE_PREFIX));
	}

	/**
	 * 获取以{@linkplain #FILE_PATH_VALUE_PREFIX}开头文件路径值的路径。
	 * 
	 * @param filePathValue
	 * @return
	 */
	protected String getFilePath(String filePathValue)
	{
		return filePathValue.substring(FILE_PATH_VALUE_PREFIX.length());
	}
}
