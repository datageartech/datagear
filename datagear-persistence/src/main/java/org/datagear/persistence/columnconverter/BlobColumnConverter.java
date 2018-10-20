/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;

/**
 * {@linkplain Blob}列值转换器。
 * <p>
 * 此类支持{@linkplain Blob}列值和{@code byte[]}、{@linkplain File}之间的相互转换。
 * </p>
 * <p>
 * 此类支持{@linkplain LOBConversionSetting#setBlobToBytesPlaceholder(byte[])}、{@linkplain LOBConversionSetting#setBlobToFilePlaceholder(File)}设置。
 * </p>
 * <p>
 * 注意：此类在将{@linkplain Blob}列值转换为{@code File}时，会在{@linkplain #getDirectory()}
 * 文件夹内创建文件，但是不会删除，为了节省磁盘资源，程序应该定时清理这个文件夹。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class BlobColumnConverter extends AbstractLOBColumnConverter
{
	public BlobColumnConverter()
	{
		super();
	}

	@Override
	public Object to(Connection cn, Model model, Property property, int propertyModelIndex, Model propertyModel,
			Object propValue) throws ColumnConverterException
	{
		if (propValue == null)
			return null;

		Class<?> propertyType = propValue.getClass();

		try
		{
			if (byte[].class.equals(propertyType))
			{
				return convertBytesToBlobColumnValue(cn, (byte[]) propValue);
			}
			else if (File.class.equals(propertyType))
			{
				return convertFileToBlobColumnValue(cn, (File) propValue);
			}
			else
				throw new ColumnConverterException("Converting from [" + propertyType.getName() + "] to ["
						+ Blob.class.getName() + "] is not supported.");
		}
		catch (SQLException e)
		{
			throw new ColumnConverterException(e);
		}
	}

	@Override
	public Object from(Connection cn, ResultSet rs, int row, int colIndex, Model model, Property property,
			int propertyModelIndex, Model propertyModel) throws ColumnConverterException
	{
		Class<?> targetType = MU.getType(propertyModel);

		Blob blob = null;

		try
		{
			blob = rs.getBlob(colIndex);

			if (byte[].class.equals(targetType))
			{
				return convertBlobColumnValueToBytes(blob);
			}
			else if (File.class.equals(targetType))
			{
				return convertBlobColumnValueToFile(blob);
			}
			else
				throw new ColumnConverterException("Converting from [" + Blob.class.getName() + "] to ["
						+ targetType.getName() + "] is not supported.");
		}
		catch (SQLException e)
		{
			throw new ColumnConverterException(e);
		}
		finally
		{
			try
			{
				blob.free();
			}
			catch (SQLException e)
			{
				throw new ColumnConverterException(e);
			}
		}
	}

	/**
	 * 将{@code byte[]}转换为{@linkplain Blob}列值。
	 * 
	 * @param cn
	 * @param bytes
	 * @return
	 * @throws SQLException
	 */
	protected Blob convertBytesToBlobColumnValue(Connection cn, byte[] bytes) throws SQLException
	{
		if (bytes == null)
			return null;

		Blob blob = cn.createBlob();

		blob.setBytes(1, bytes);

		return blob;
	}

	/**
	 * 将{@linkplain File}转换为{@linkplain Blob}列值。
	 * 
	 * @param cn
	 * @param file
	 * @return
	 * @throws SQLException
	 */
	protected Blob convertFileToBlobColumnValue(Connection cn, File file) throws SQLException
	{
		if (file == null)
			return null;

		Blob blob = cn.createBlob();

		OutputStream blobOut = null;
		InputStream fileIn = null;

		try
		{
			blobOut = blob.setBinaryStream(1);
			fileIn = new BufferedInputStream(new FileInputStream(file));

			write(fileIn, blobOut);
		}
		catch (IOException e)
		{
			throw new ColumnConverterException(e);
		}
		finally
		{
			close(fileIn);
			close(blobOut);
		}

		return blob;
	}

	/**
	 * 将{@linkplain Blob}列值转换为{@code byte[]}。
	 * 
	 * @param blob
	 * @return
	 * @throws SQLException
	 */
	protected byte[] convertBlobColumnValueToBytes(Blob blob) throws SQLException
	{
		if (blob == null)
			return null;

		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		if (conversionSetting.isBlobToBytesPlaceholder())
		{
			return conversionSetting.getBlobToBytesPlaceholder();
		}
		else
		{
			long length = blob.length();

			if (length > Integer.MAX_VALUE)
				throw new ColumnConverterException("The " + Blob.class.getName() + "[length=" + length
						+ "] 's length is too long for converting to " + byte[].class.getName());

			return blob.getBytes(1L, (int) length);
		}
	}

	/**
	 * 将{@linkplain Blob}列值转换为{@linkplain File}。
	 * 
	 * @param blob
	 * @return
	 * @throws SQLException
	 */
	protected File convertBlobColumnValueToFile(Blob blob) throws SQLException
	{
		if (blob == null)
			return null;

		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		if (conversionSetting.isBlobToFilePlaceholder())
		{
			return conversionSetting.getBlobToFilePlaceholder();
		}
		else
		{
			InputStream blobIn = null;
			OutputStream fileOut = null;

			File file = new File(getDirectory(), uuid() + ".blob");

			try
			{
				blobIn = blob.getBinaryStream();
				fileOut = new BufferedOutputStream(new FileOutputStream(file));

				write(blobIn, fileOut);

				return file;
			}
			catch (IOException e)
			{
				throw new ColumnConverterException(e);
			}
			finally
			{
				close(blobIn);
				close(fileOut);
			}
		}
	}
}
