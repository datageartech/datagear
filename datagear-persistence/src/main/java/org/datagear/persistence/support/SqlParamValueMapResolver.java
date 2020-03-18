/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * SQL参数值处理器。
 * <p>
 * 此类用于为{@linkplain ConversionSqlParamValueMapper}提供字符串特殊格式、文件、输入流处理相关支持。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlParamValueMapResolver
{
	/** 文件路径值前缀 */
	public static final String PREFIX_FILE_PATH = "file:";

	/** Hex值前缀 */
	public static final String PREFIX_HEX = "hex:";

	/** Base64值前缀 */
	public static final String PREFIX_BASE64 = "base64:";

	/** 文件的字符集 */
	private String fileCharset;

	public SqlParamValueMapResolver()
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
	 * 获取指定字符串所表示的字节数组。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return 返回{@code null}表示不是字节数组
	 * @throws SqlParamValueMapperException
	 */
	public byte[] getBytes(Table table, Column column, String value) throws SqlParamValueMapperException
	{
		if (!isBytes(value))
			return null;

		return toBytes(value);
	}

	/**
	 * 获取指定字符串所表示的文件。
	 * 
	 * @param table
	 * @param column
	 * @param value
	 * @return 返回{@code null}表示不是文件
	 * @throws SqlParamValueMapperException
	 */
	public File getFile(Table table, Column column, String value) throws SqlParamValueMapperException
	{
		if (!isFile(value))
			return null;

		File file = new File(getFilePath(value));

		return (file.exists() ? file : null);
	}

	/**
	 * 获取文件输入流。
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
	 * 获取文件输入流。
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
	 * 给定字符串是否表示文件。
	 * 
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	public boolean isFile(String value) throws SqlParamValueMapperException
	{
		return (value != null && value.startsWith(PREFIX_FILE_PATH));
	}

	/**
	 * 给定字符串是否表示字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	public boolean isBytes(String value) throws SqlParamValueMapperException
	{
		if (value == null)
			return false;

		return (value.startsWith(PREFIX_HEX) || value.startsWith(PREFIX_BASE64));
	}

	/**
	 * 获取{@linkplain #isFile(String)}字符串的文件路径。
	 * 
	 * @param filePathValue
	 * @return
	 */
	protected String getFilePath(String filePathValue)
	{
		return filePathValue.substring(PREFIX_FILE_PATH.length());
	}

	/**
	 * 将{@linkplain #isBytes(String)}字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws SqlParamValueMapperException
	 */
	protected byte[] toBytes(String value) throws SqlParamValueMapperException
	{
		byte[] bytes = null;

		try
		{
			if (value.startsWith(PREFIX_HEX))
				bytes = convertToBytesForHex(value.substring(PREFIX_HEX.length()));
			else if (value.startsWith(PREFIX_BASE64))
				bytes = convertToBytesForBase64(value.substring(PREFIX_BASE64.length()));

			return bytes;
		}
		catch(SqlParamValueMapperException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			throw new SqlParamValueMapperException(t);
		}
	}

	/**
	 * 将Hex编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	protected byte[] convertToBytesForHex(String value) throws DecoderException
	{
		if (value == null || value.isEmpty())
			return null;

		if (value.startsWith("0x") || value.startsWith("0X"))
			value = value.substring(2);

		return Hex.decodeHex(value);
	}

	/**
	 * 将Base64编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] convertToBytesForBase64(String value)
	{
		if (value == null || value.isEmpty())
			return null;

		return Base64.getDecoder().decode(value);
	}
}
