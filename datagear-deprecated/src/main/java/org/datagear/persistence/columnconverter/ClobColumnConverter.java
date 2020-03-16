/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.model.support.MU;
import org.datagear.persistence.columnconverter.LOBConversionContext.LOBConversionSetting;

/**
 * {@linkplain Clob}列值转换器。
 * <p>
 * 此类支持{@linkplain Clob}列值和{@linkplain String}、{@linkplain File}、{@linkplain FileEncoding}之间的相互转换。
 * </p>
 * <p>
 * 此类支持{@linkplain LOBConversionSetting#setLeftClobLengthOnReading(int)}、
 * {@linkplain LOBConversionSetting#setFileToClobEncoding(String)}、
 * {@linkplain LOBConversionSetting#setClobToFileEncoding(String)}设置。
 * </p>
 * <p>
 * 注意：此类在将{@linkplain Clob}列值转换为{@code File}时，会在{@linkplain #getDirectory()}
 * 文件夹内创建文件，但是不会删除，为了节省磁盘资源，程序应该定时清理这个文件夹。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ClobColumnConverter extends AbstractLOBColumnConverter
{
	public ClobColumnConverter()
	{
		super();
	}

	@Override
	public Object to(Connection cn, Model model, Property property, Object propValue) throws ColumnConverterException
	{
		if (propValue == null)
			return null;

		Class<?> propertyType = propValue.getClass();

		try
		{
			if (String.class.equals(propertyType))
			{
				return convertStringToClobColumnValue(cn, (String) propValue);
			}
			else if (FileEncoding.class.equals(propertyType))
			{
				FileEncoding fileEncoding = (FileEncoding) propValue;

				return convertFileToClobColumnValue(cn, fileEncoding.getFile(), fileEncoding.getEncoding());
			}
			else if (File.class.equals(propertyType))
			{
				return convertFileToClobColumnValue(cn, (File) propValue, null);
			}
			else
				throw new ColumnConverterException("Converting from [" + propertyType.getName() + "] to ["
						+ Clob.class.getName() + "] is not supported.");
		}
		catch (SQLException e)
		{
			throw new ColumnConverterException(e);
		}
	}

	@Override
	public Object from(Connection cn, ResultSet rs, int row, int colIndex, Model model, Property property)
			throws ColumnConverterException
	{
		Class<?> targetType = MU.getType(property);

		Clob clob = null;

		try
		{
			clob = rs.getClob(colIndex);

			if (rs.wasNull())
				clob = null;

			if (clob == null)
			{
				return null;
			}
			else if (String.class.equals(targetType))
			{
				return convertClobColumnValueToString(clob);
			}
			else if (File.class.equals(targetType))
			{
				return convertClobColumnValueToFile(clob);
			}
			else
				throw new ColumnConverterException("Converting from [" + Clob.class.getName() + "] to ["
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
				if (clob != null)
					clob.free();
			}
			catch (SQLException e)
			{
				throw new ColumnConverterException(e);
			}
		}
	}

	/**
	 * 将{@linkplain String}转换为{@linkplain Clob}列值。
	 * 
	 * @param cn
	 * @param str
	 * @return
	 * @throws SQLException
	 */
	protected Clob convertStringToClobColumnValue(Connection cn, String str) throws SQLException
	{
		if (str == null)
			return null;

		Clob clob = cn.createClob();

		clob.setString(1, str);

		return clob;
	}

	/**
	 * 将{@linkplain File}转换为{@linkplain Clob}列值。
	 * 
	 * @param cn
	 * @param file
	 * @param fileEncoding
	 *            允许为{@code null}。
	 * @return
	 * @throws SQLException
	 */
	protected Clob convertFileToClobColumnValue(Connection cn, File file, String fileEncoding) throws SQLException
	{
		if (file == null)
			return null;

		Clob clob = cn.createClob();

		Writer clobOut = null;
		Reader fileIn = null;

		try
		{
			clobOut = clob.setCharacterStream(1);

			if (fileEncoding == null || fileEncoding.isEmpty())
				fileIn = new BufferedReader(new FileReader(file));
			else
				fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileEncoding));

			write(fileIn, clobOut);
		}
		catch (IOException e)
		{
			throw new ColumnConverterException(e);
		}
		finally
		{
			close(fileIn);
			close(clobOut);
		}

		return clob;
	}

	/**
	 * 将{@linkplain Clob}列值转换为{@linkplain String}。
	 * 
	 * @param clob
	 * @return
	 * @throws SQLException
	 */
	protected String convertClobColumnValueToString(Clob clob) throws SQLException
	{
		if (clob == null)
			return null;

		long length = clob.length();

		LOBConversionSetting conversionSetting = LOBConversionContext.get();
		if (conversionSetting.isLeftClobOnReading() && conversionSetting.getLeftClobLengthOnReading() < length)
			length = conversionSetting.getLeftClobLengthOnReading();

		if (length > Integer.MAX_VALUE)
			throw new ColumnConverterException("The " + Clob.class.getName() + "[length=" + length
					+ "] 's length is too long for converting to [" + String.class.getName() + "]");

		return clob.getSubString(1, (int) length);
	}

	/**
	 * 将{@linkplain Clob}列值转换为{@linkplain File}。
	 * 
	 * @param clob
	 * @return
	 * @throws SQLException
	 */
	protected File convertClobColumnValueToFile(Clob clob) throws SQLException
	{
		if (clob == null)
			return null;

		long length = clob.length();

		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		Reader clobIn = null;
		Writer fileOut = null;

		File file = new File(getDirectory(), uuid() + ".txt");

		try
		{
			clobIn = clob.getCharacterStream();

			if (conversionSetting.hasClobToFileEncoding())
				fileOut = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file), conversionSetting.getClobToFileEncoding()));
			else
				fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

			if (conversionSetting.isLeftClobOnReading() && conversionSetting.getLeftClobLengthOnReading() < length)
				write(clobIn, fileOut, conversionSetting.getLeftClobLengthOnReading());
			else
				write(clobIn, fileOut);

			return file;
		}
		catch (IOException e)
		{
			throw new ColumnConverterException(e);
		}
		finally
		{
			close(clobIn);
			close(fileOut);
		}
	}
}
