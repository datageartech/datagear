/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.datagear.dataexchange.DataFormat;
import org.datagear.dataexchange.DataFormat.BinaryFormat;

/**
 * 数据格式上下文。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormatContext
{
	private DataFormat dataFormat;

	private DateFormat dateFormatter;

	private DateFormat timeFormatter;

	private DateFormat timestampFormatter;

	private NumberFormat numberFormatter;

	public DataFormatContext()
	{
		super();
	}

	public DataFormatContext(DataFormat dataFormat)
	{
		super();
		setDataFormat(dataFormat);
	}

	public DataFormat getDataFormat()
	{
		return dataFormat;
	}

	public void setDataFormat(DataFormat dataFormat)
	{
		this.dataFormat = dataFormat;

		this.dateFormatter = new SimpleDateFormat(dataFormat.getDateFormat(), dataFormat.getLocale());
		this.timeFormatter = new SimpleDateFormat(dataFormat.getTimeFormat(), dataFormat.getLocale());
		this.timestampFormatter = new SimpleDateFormat(dataFormat.getTimestampFormat(), dataFormat.getLocale());
		this.numberFormatter = new DecimalFormat(dataFormat.getNumberFormat(),
				DecimalFormatSymbols.getInstance(dataFormat.getLocale()));
	}

	public DateFormat getDateFormatter()
	{
		return dateFormatter;
	}

	public void setDateFormatter(DateFormat dateFormatter)
	{
		this.dateFormatter = dateFormatter;
	}

	public DateFormat getTimeFormatter()
	{
		return timeFormatter;
	}

	public void setTimeFormatter(DateFormat timeFormatter)
	{
		this.timeFormatter = timeFormatter;
	}

	public DateFormat getTimestampFormatter()
	{
		return timestampFormatter;
	}

	public void setTimestampFormatter(DateFormat timestampFormatter)
	{
		this.timestampFormatter = timestampFormatter;
	}

	public NumberFormat getNumberFormatter()
	{
		return numberFormatter;
	}

	public void setNumberFormatter(NumberFormat numberFormatter)
	{
		this.numberFormatter = numberFormatter;
	}

	/**
	 * 解析{@linkplain Date}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public Date parseDate(String value) throws ParseException
	{
		java.util.Date d = this.dateFormatter.parse(value);
		return new Date(d.getTime());
	}

	/**
	 * 解析{@linkplain Time}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public Time parseTime(String value) throws ParseException
	{
		java.util.Date d = this.timeFormatter.parse(value);
		return new Time(d.getTime());
	}

	/**
	 * 解析{@linkplain Timestamp}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public Timestamp parseTimestamp(String value) throws ParseException
	{
		Timestamp ts = null;

		// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
		if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(this.dataFormat.getTimestampFormat()))
		{
			ts = Timestamp.valueOf(value);
		}
		else
		{
			// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决
			java.util.Date tsdv = this.timeFormatter.parse(value);
			ts = new Timestamp(tsdv.getTime());
		}

		return ts;
	}

	/**
	 * 解析{@code int}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public int parseInt(String value) throws ParseException
	{
		this.numberFormatter.setParseIntegerOnly(true);
		return this.numberFormatter.parse(value).intValue();
	}

	/**
	 * 解析{@code long}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public long parseLong(String value) throws ParseException
	{
		this.numberFormatter.setParseIntegerOnly(true);
		return this.numberFormatter.parse(value).longValue();
	}

	/**
	 * 解析{@code float}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public float parseFloat(String value) throws ParseException
	{
		this.numberFormatter.setParseIntegerOnly(false);
		return this.numberFormatter.parse(value).floatValue();
	}

	/**
	 * 解析{@code double}。
	 * 
	 * @param value
	 * @return
	 * @throws ParseException
	 */
	public double parseDouble(String value) throws ParseException
	{
		this.numberFormatter.setParseIntegerOnly(false);
		return this.numberFormatter.parse(value).doubleValue();
	}

	/**
	 * 解析字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	public byte[] parseBytes(String value) throws DecoderException
	{
		byte[] bytes = null;

		BinaryFormat binaryFormat = this.dataFormat.getBinaryFormat();
		if (BinaryFormat.NULL.equals(binaryFormat))
		{
			bytes = null;
		}
		else if (BinaryFormat.HEX.equals(binaryFormat))
		{
			bytes = convertToBytesForHex(value);
		}
		else if (BinaryFormat.BASE64.equals(binaryFormat))
		{
			bytes = convertToBytesForBase64(value);
		}
		else
			throw new UnsupportedOperationException();

		return bytes;
	}

	/**
	 * 格式化{@linkplain Date}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatDate(Date value)
	{
		return this.dateFormatter.format(value);
	}

	/**
	 * 格式化{@linkplain Time}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatTime(Time value)
	{
		return this.timeFormatter.format(value);
	}

	/**
	 * 格式化{@linkplain Timestamp}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatTimestamp(Timestamp value)
	{
		String valueStr = null;

		// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
		if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(dataFormat.getTimestampFormat()))
		{
			valueStr = value.toString();
		}
		else
		{
			// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决

			valueStr = this.timestampFormatter.format(value);
		}

		return valueStr;
	}

	/**
	 * 格式化{@code int}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatInt(int value)
	{
		return this.numberFormatter.format(value);
	}

	/**
	 * 格式化{@code long}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatLong(long value)
	{
		return this.numberFormatter.format(value);
	}

	/**
	 * 格式化{@code float}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatFloat(float value)
	{
		return this.numberFormatter.format(value);
	}

	/**
	 * 格式化{@code double}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatDouble(double value)
	{
		return this.numberFormatter.format(value);
	}

	/**
	 * 格式化字节数组。
	 * 
	 * @param value
	 * @return
	 */
	public String formatBytes(byte[] value)
	{
		String str = null;

		BinaryFormat binaryFormat = this.dataFormat.getBinaryFormat();

		if (value == null)
			;
		else if (BinaryFormat.NULL.equals(binaryFormat))
			;
		else if (BinaryFormat.HEX.equals(binaryFormat))
			str = convertToHex(value);
		else if (BinaryFormat.BASE64.equals(binaryFormat))
			str = convertToBase64(value);
		else
			throw new UnsupportedOperationException();

		return str;
	}

	/**
	 * 将字节数组编码转换为字符串。
	 * 
	 * @param bytes
	 * @param binaryFormat
	 * @return
	 */
	protected String convertToString(byte[] bytes, BinaryFormat binaryFormat)
	{
		String value = null;

		if (bytes == null)
			;
		else if (BinaryFormat.NULL.equals(binaryFormat))
			;
		else if (BinaryFormat.HEX.equals(binaryFormat))
			value = convertToHex(bytes);
		else if (BinaryFormat.BASE64.equals(binaryFormat))
			value = convertToBase64(bytes);
		else
			throw new UnsupportedOperationException();

		return value;
	}

	/**
	 * 将字节数组转换为Hex字符串。
	 * 
	 * @param bytes
	 * @return
	 */
	protected String convertToHex(byte[] bytes)
	{
		if (bytes == null)
			return null;

		return Hex.encodeHexString(bytes);
	}

	/**
	 * 将字节数组转换为Base64字符串。
	 * 
	 * @param bytes
	 * @return
	 */
	protected String convertToBase64(byte[] bytes)
	{
		if (bytes == null)
			return null;

		return Base64.encodeBase64String(bytes);
	}

	/**
	 * 将HEX编码的字符串转换为字节数组。
	 * 
	 * @param value
	 * @return
	 * @throws DecoderException
	 */
	protected byte[] convertToBytesForHex(String value) throws DecoderException
	{
		if (value == null || value.isEmpty())
			return null;

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

		return Base64.decodeBase64(value);
	}
}
