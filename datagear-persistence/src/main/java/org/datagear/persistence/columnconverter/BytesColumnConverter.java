/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;

/**
 * byte[]列值转换器。
 * <p>
 * 此类支持{@code byte[]}列值和{@code byte[]}、{@linkplain File}之间的相互转换。
 * </p>
 * <p>
 * 此类支持{@linkplain LOBConversionSetting#setPlaceholdBlobOnReading(boolean)}设置。
 * </p>
 * <p>
 * 注意：此类在将{@code byte[]}列值转换为{@code File}时，会在{@linkplain #getDirectory()}
 * 文件夹内创建文件，但是不会删除，为了节省磁盘资源，程序应该定时清理这个文件夹。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class BytesColumnConverter extends AbstractLOBColumnConverter
{
	public BytesColumnConverter()
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
				return propValue;
			}
			else if (File.class.equals(propertyType))
			{
				return convertFileToBytesColumnValue(cn, (File) propValue);
			}
			else
				throw new ColumnConverterException("Converting from [" + propertyType.getName() + "] to ["
						+ byte[].class.getName() + "] is not supported.");
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

		byte[] bytes = null;

		try
		{
			bytes = rs.getBytes(colIndex);

			if (byte[].class.equals(targetType))
			{
				return convertBytesColumnValueToBytes(bytes);
			}
			else if (File.class.equals(targetType))
			{
				return convertBytesColumnValueToFile(bytes);
			}
			else
				throw new ColumnConverterException("Converting from [" + byte[].class.getName() + "] to ["
						+ targetType.getName() + "] is not supported.");
		}
		catch (SQLException e)
		{
			throw new ColumnConverterException(e);
		}
	}

	/**
	 * 将{@linkplain File}转换为{@linkplain byte[]}列值。
	 * 
	 * @param cn
	 * @param file
	 * @return
	 * @throws SQLException
	 */
	protected byte[] convertFileToBytesColumnValue(Connection cn, File file) throws SQLException
	{
		if (file == null)
			return null;

		long length = file.length();

		if (length > Integer.MAX_VALUE)
			throw new ColumnConverterException("The [" + file + "] 's length [" + length
					+ "] is too long for converting to " + byte[].class.getName());

		ByteArrayOutputStream out = null;
		InputStream in = null;

		try
		{
			out = new ByteArrayOutputStream();
			in = new BufferedInputStream(new FileInputStream(file));

			write(in, out);
		}
		catch (IOException e)
		{
			throw new ColumnConverterException(e);
		}
		finally
		{
			close(in);
			close(out);
		}

		return out.toByteArray();
	}

	/**
	 * 将{@code byte[]}列值转换为{@code byte[]}。
	 * 
	 * @param bytes
	 * @return
	 * @throws SQLException
	 */
	protected byte[] convertBytesColumnValueToBytes(byte[] bytes) throws SQLException
	{
		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		if (conversionSetting.isBlobToBytesPlaceholder())
		{
			return conversionSetting.getBlobToBytesPlaceholder();
		}
		else
		{
			return bytes;
		}
	}

	/**
	 * 将{@code byte[]}列值转换为{@linkplain File}。
	 * 
	 * @param bytes
	 * @return
	 * @throws SQLException
	 */
	protected File convertBytesColumnValueToFile(byte[] bytes) throws SQLException
	{
		if (bytes == null)
			return null;

		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		if (conversionSetting.isBlobToFilePlaceholder())
		{
			return conversionSetting.getBlobToFilePlaceholder();
		}
		else
		{
			OutputStream out = null;

			File file = new File(getDirectory(), uuid());

			try
			{
				out = new BufferedOutputStream(new FileOutputStream(file));

				out.write(bytes, 0, bytes.length);

				return file;
			}
			catch (IOException e)
			{
				throw new ColumnConverterException(e);
			}
			finally
			{
				close(out);
			}
		}
	}
}
