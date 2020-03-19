/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.persistence.support;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLXML;
import java.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 默认{@linkplain AbstractLOBRowMapper}。
 * <p>
 * 默认地，对于二进制类型，返回{@code byte[]}；对于大字符类型，返回{@linkplain String}；其他，返回默认值。
 * </p>
 * <p>
 * 可以通过{@linkplain #setReadActualLobRows(int)}、{@linkplain #setBinaryEncoder(String)}、
 * {@linkplain #setBigBinaryDirectory(File)}来控制映射格式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DefaultLOBRowMapper extends AbstractLOBRowMapper
{
	public static final String DEFAULT_BLOB_PLACEHOLDER = "[BLOB]";

	public static final String DEFAULT_CLOB_PLACEHOLDER = "[CLOB]";

	public static final String DEFAULT_SQL_XML_PLACEHOLDER = "[XML]";

	public static final String BINARY_ENCODER_HEX = "hex";

	public static final String BINARY_ENCODER_BASE64 = "base64";

	public static final String BINARY_ENCODER_NONE = "none";

	/** 读取真实LOB数据的最大行数，-1表示全部 */
	private int readActualLobRows = -1;

	/** BLOB占位符字符串 */
	private String blobPlaceholder = DEFAULT_BLOB_PLACEHOLDER;

	/** CLOB占位符字符串 */
	private String clobPlaceholder = DEFAULT_CLOB_PLACEHOLDER;

	/** SQLXML占位符字符串 */
	private String sqlXmlPlaceholder = DEFAULT_SQL_XML_PLACEHOLDER;

	/** 二进制编码 */
	private String binaryEncoder = BINARY_ENCODER_NONE;

	/** HEX编码开头 */
	private String binaryHexEncoderHeader = "0x";

	/** 大二进制不返回byte[]而写入文件的父目录 */
	private File bigBinaryDirectory = null;

	public DefaultLOBRowMapper()
	{
	}

	public boolean isReadActualLobAll()
	{
		return this.readActualLobRows < 0;
	}

	public boolean isReadActualLobRow(int rowIndex)
	{
		return (isReadActualLobAll() || rowIndex <= this.readActualLobRows);
	}

	public int getReadActualLobRows()
	{
		return readActualLobRows;
	}

	/**
	 * 设置读取真实LOB数据的最大行数，超过这个行数的将被占位符字符串替代，-1表示全部。
	 * <p>
	 * 默认为{@code -1}。
	 * </p>
	 * 
	 * @param readActualLobRows
	 */
	public void setReadActualLobRows(int readActualLobRows)
	{
		this.readActualLobRows = readActualLobRows;
	}

	public String getBlobPlaceholder()
	{
		return blobPlaceholder;
	}

	public void setBlobPlaceholder(String blobPlaceholder)
	{
		this.blobPlaceholder = blobPlaceholder;
	}

	public String getClobPlaceholder()
	{
		return clobPlaceholder;
	}

	public void setClobPlaceholder(String clobPlaceholder)
	{
		this.clobPlaceholder = clobPlaceholder;
	}

	public String getSqlXmlPlaceholder()
	{
		return sqlXmlPlaceholder;
	}

	public void setSqlXmlPlaceholder(String sqlXmlPlaceholder)
	{
		this.sqlXmlPlaceholder = sqlXmlPlaceholder;
	}

	public String getBinaryEncoder()
	{
		return binaryEncoder;
	}

	/**
	 * 设置二进制编码格式。
	 * <p>
	 * 默认为{@linkplain #BINARY_ENCODER_NONE}。
	 * </p>
	 * 
	 * @param binaryEncoder
	 */
	public void setBinaryEncoder(String binaryEncoder)
	{
		this.binaryEncoder = binaryEncoder;
	}

	public String getBinaryHexEncoderHeader()
	{
		return binaryHexEncoderHeader;
	}

	public void setBinaryHexEncoderHeader(String binaryHexEncoderHeader)
	{
		this.binaryHexEncoderHeader = binaryHexEncoderHeader;
	}

	public boolean hasBigBinaryDirectory()
	{
		return (this.bigBinaryDirectory != null);
	}

	public File getBigBinaryDirectory()
	{
		return bigBinaryDirectory;
	}

	/**
	 * 设置大二进制数据的写入为文件的父目录，为{@code null}，则大二进制数据直接返回{@code byte[]}。
	 * <p>
	 * 设置后，映射结果值将是这个目录下的文件名字符串。
	 * </p>
	 * <p>
	 * 默认为{@code null}。
	 * </p>
	 * 
	 * @param bigBinaryDirectory
	 */
	public void setBigBinaryDirectory(File bigBinaryDirectory)
	{
		this.bigBinaryDirectory = bigBinaryDirectory;
	}

	@Override
	protected Object mapColumnLONGVARCHAR(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		Reader value = rs.getCharacterStream(column.getName());

		if (isNullValue(rs, value))
		{
			IOUtil.close(value);
			return null;
		}

		return mapColumnForLargeReaderValue(cn, table, rs, rowIndex, column, value, this.clobPlaceholder);
	}

	@Override
	protected Object mapColumnBINARY(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		byte[] value = rs.getBytes(column.getName());

		if (isNullValue(rs, value))
			return null;

		return encodeBytesToStringIf(value);
	}

	@Override
	protected Object mapColumnLONGVARBINARY(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		InputStream value = rs.getBinaryStream(column.getName());

		if (isNullValue(rs, value))
		{
			IOUtil.close(value);
			return null;
		}

		return mapColumnForLargeInputStreamValue(cn, table, rs, rowIndex, column, value, this.blobPlaceholder);
	}

	@Override
	protected Object mapColumnCLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		Clob value = rs.getClob(column.getName());

		if (isNullValue(rs, value))
			return null;

		Reader in = value.getCharacterStream();
		return mapColumnForLargeReaderValue(cn, table, rs, rowIndex, column, in, this.clobPlaceholder);
	}

	@Override
	protected Object mapColumnBLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		Blob value = rs.getBlob(column.getName());

		if (isNullValue(rs, value))
			return null;

		InputStream in = value.getBinaryStream();
		return mapColumnForLargeInputStreamValue(cn, table, rs, rowIndex, column, in, this.blobPlaceholder);
	}

	@Override
	protected Object mapColumnLONGNVARCHAR(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		Reader value = rs.getCharacterStream(column.getName());

		if (isNullValue(rs, value))
		{
			IOUtil.close(value);
			return null;
		}

		return mapColumnForLargeReaderValue(cn, table, rs, rowIndex, column, value, this.clobPlaceholder);
	}

	@Override
	protected Object mapColumnNCLOB(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		NClob value = rs.getNClob(column.getName());

		if (isNullValue(rs, value))
			return null;

		Reader in = value.getCharacterStream();
		return mapColumnForLargeReaderValue(cn, table, rs, rowIndex, column, in, this.clobPlaceholder);
	}

	@Override
	protected Object mapColumnSQLXML(Connection cn, Table table, ResultSet rs, int rowIndex, Column column)
			throws Throwable
	{
		SQLXML value = rs.getSQLXML(column.getName());

		if (isNullValue(rs, value))
			return null;

		Reader in = value.getCharacterStream();
		return mapColumnForLargeReaderValue(cn, table, rs, rowIndex, column, in, this.sqlXmlPlaceholder);
	}

	protected Object mapColumnForLargeReaderValue(Connection cn, Table table, ResultSet rs, int rowIndex, Column column,
			Reader value, String placeholder) throws Throwable
	{
		try
		{
			if (isReadActualLobRow(rowIndex))
				return IOUtil.readString(value, false);
			else
				return placeholder;
		}
		finally
		{
			IOUtil.close(value);
		}
	}

	protected Object mapColumnForLargeInputStreamValue(Connection cn, Table table, ResultSet rs, int rowIndex,
			Column column, InputStream value, String placeholder) throws Throwable
	{
		try
		{
			if (isReadActualLobRow(rowIndex))
			{
				if (this.bigBinaryDirectory != null)
				{
					String fileName = IDUtil.uuid();
					File file = FileUtil.getFile(this.bigBinaryDirectory, fileName);

					IOUtil.write(value, file);
					return fileName;
				}
				else
				{
					byte[] bytes = IOUtil.getBytes(value);
					return encodeBytesToStringIf(bytes);
				}
			}
			else
				return placeholder;
		}
		finally
		{
			IOUtil.close(value);
		}
	}

	protected Object encodeBytesToStringIf(byte[] value) throws Throwable
	{
		if (value == null)
			return null;

		if (BINARY_ENCODER_HEX.equals(this.binaryEncoder))
		{
			String hex = Hex.encodeHexString(value);

			if (!StringUtil.isEmpty(this.binaryHexEncoderHeader))
				hex = this.binaryHexEncoderHeader + hex;

			return hex;
		}
		else if (BINARY_ENCODER_BASE64.equals(this.binaryEncoder))
			return Base64.getEncoder().encodeToString(value);
		else
			return value;
	}
}
