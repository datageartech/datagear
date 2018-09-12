/*
 * Copyright (c) 2018 by datagear.org.
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
import java.io.StringReader;
import java.io.StringWriter;
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
 * {@linkplain String}列值转换器。
 * <p>
 * 此类支持{@linkplain String}列值和{@linkplain String}、{@linkplain File}、{@linkplain FileEncoding}之间的相互转换。
 * </p>
 * <p>
 * 此类支持{@linkplain LOBConversionSetting#setLeftClobLengthOnReading(int)}、
 * {@linkplain LOBConversionSetting#setFileToClobEncoding(String)}、
 * {@linkplain LOBConversionSetting#setClobToFileEncoding(String)}设置。
 * </p>
 * <p>
 * 注意：此类在将{@linkplain String}列值转换为{@code File}时，会在{@linkplain #getDirectory()}
 * 文件夹内创建文件，但是不会删除，为了节省磁盘资源，程序应该定时清理这个文件夹。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class StringColumnConverter extends AbstractLOBColumnConverter
{
	public StringColumnConverter()
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
			if (String.class.equals(propertyType))
			{
				return convertStringToStringColumnValue(cn, (String) propValue);
			}
			else if (FileEncoding.class.equals(propertyType))
			{
				FileEncoding fileEncoding = (FileEncoding) propValue;

				return convertFileToStringColumnValue(cn, fileEncoding.getFile(), fileEncoding.getEncoding());
			}
			else if (File.class.equals(propertyType))
			{
				return convertFileToStringColumnValue(cn, (File) propValue, null);
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
	public Object from(Connection cn, ResultSet rs, int row, int colIndex, Model model, Property property,
			int propertyModelIndex, Model propertyModel) throws ColumnConverterException
	{
		Class<?> targetType = MU.getType(propertyModel);

		String columnValue = null;

		try
		{
			columnValue = rs.getString(colIndex);

			if (String.class.equals(targetType))
			{
				return convertStringColumnValueToString(columnValue);
			}
			else if (File.class.equals(targetType))
			{
				return convertStringColumnValueToFile(columnValue);
			}
			else
				throw new ColumnConverterException("Converting from [" + String.class.getName() + "] to ["
						+ targetType.getName() + "] is not supported.");
		}
		catch (SQLException e)
		{
			throw new ColumnConverterException(e);
		}
	}

	/**
	 * 将{@linkplain String}转换为{@linkplain String}列值。
	 * 
	 * @param cn
	 * @param str
	 * @return
	 * @throws SQLException
	 */
	protected String convertStringToStringColumnValue(Connection cn, String str) throws SQLException
	{
		return str;
	}

	/**
	 * 将{@linkplain File}转换为{@linkplain String}列值。
	 * 
	 * @param cn
	 * @param file
	 * @param fileEncoding
	 *            允许为{@code null}。
	 * @return
	 * @throws SQLException
	 */
	protected String convertFileToStringColumnValue(Connection cn, File file, String fileEncoding) throws SQLException
	{
		if (file == null)
			return null;

		Writer out = null;
		Reader in = null;

		try
		{
			out = new StringWriter();

			if (fileEncoding == null || fileEncoding.isEmpty())
				in = new BufferedReader(new FileReader(file));
			else
				in = new BufferedReader(new InputStreamReader(new FileInputStream(file), fileEncoding));

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

		return out.toString();
	}

	/**
	 * 将{@linkplain String}列值转换为{@linkplain String}。
	 * 
	 * @param columnValue
	 * @return
	 * @throws SQLException
	 */
	protected String convertStringColumnValueToString(String columnValue) throws SQLException
	{
		if (columnValue == null)
			return null;

		long length = columnValue.length();

		LOBConversionSetting conversionSetting = LOBConversionContext.get();
		if (conversionSetting.isLeftClobOnReading() && conversionSetting.getLeftClobLengthOnReading() < length)
			length = conversionSetting.getLeftClobLengthOnReading();

		if (length > Integer.MAX_VALUE)
			throw new ColumnConverterException("The " + Clob.class.getName() + "[length=" + length
					+ "] 's length is too long for converting to [" + String.class.getName() + "]");

		return columnValue.substring(0, (int) length);
	}

	/**
	 * 将{@linkplain String}列值转换为{@linkplain File}。
	 * 
	 * @param columnValue
	 * @return
	 * @throws SQLException
	 */
	protected File convertStringColumnValueToFile(String columnValue) throws SQLException
	{
		if (columnValue == null)
			return null;

		long length = columnValue.length();

		LOBConversionSetting conversionSetting = LOBConversionContext.get();

		Reader in = null;
		Writer out = null;

		File file = new File(getDirectory(), uuid() + ".txt");

		try
		{
			in = new StringReader(columnValue);

			if (conversionSetting.hasClobToFileEncoding())
				out = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(file), conversionSetting.getClobToFileEncoding()));
			else
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

			if (conversionSetting.isLeftClobOnReading() && conversionSetting.getLeftClobLengthOnReading() < length)
				write(in, out, conversionSetting.getLeftClobLengthOnReading());
			else
				write(in, out);

			return file;
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
	}
}
