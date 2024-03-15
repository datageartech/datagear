/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.dataexchange;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.datagear.util.NumberParser;
import org.datagear.util.NumberParserException;
import org.datagear.util.expression.Expression;
import org.datagear.util.expression.ExpressionResolver;

/**
 * 数据格式上下文。
 * 
 * @author datagear@163.com
 *
 */
public class DataFormatContext extends NumberParser
{
	public static final String EXP_START_IDENTIFIER = ExpressionResolver.DEFAULT_START_IDENTIFIER_DOLLAR;

	public static final String EXP_END_IDENTIFIER = ExpressionResolver.DEFAULT_END_IDENTIFIER;

	private DataFormat dataFormat;

	private ExpressionResolver expressionResolver = new DataFormatExpressionResolver();

	private Expression _dateExpression;
	private Expression _timeExpression;
	private Expression _timestampExpression;
	private Expression _numberExpression;
	private Expression _binaryExpression;

	private DateFormat _dateFormat;
	private DateFormat _timeFormat;
	private DateFormat _timestampFormat;
	private DecimalFormat _numberFormat;

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

		this._dateExpression = this.expressionResolver.resolveFirst(dataFormat.getDateFormat());
		this._timeExpression = this.expressionResolver.resolveFirst(dataFormat.getTimeFormat());
		this._timestampExpression = this.expressionResolver.resolveFirst(dataFormat.getTimestampFormat());
		this._numberExpression = this.expressionResolver.resolveFirst(dataFormat.getNumberFormat());
		this._binaryExpression = this.expressionResolver.resolveFirst(dataFormat.getBinaryFormat());

		this._dateFormat = new SimpleDateFormat(getDatePattern(), dataFormat.getLocale());
		this._timeFormat = new SimpleDateFormat(getTimePattern(), dataFormat.getLocale());
		this._timestampFormat = new SimpleDateFormat(getTimestampPattern(), dataFormat.getLocale());
		this._numberFormat = new DecimalFormat(getNumberPattern(),
				DecimalFormatSymbols.getInstance(dataFormat.getLocale()));
		// 始终解析为BigDecimal，因为源数据范围未知
		this._numberFormat.setParseBigDecimal(true);
	}

	public ExpressionResolver getExpressionResolver()
	{
		return expressionResolver;
	}

	public void setExpressionResolver(ExpressionResolver expressionResolver)
	{
		this.expressionResolver = expressionResolver;
	}

	/**
	 * 是否是纯日期格式，而非表达式。
	 * 
	 * @return
	 */
	public boolean isPureDatePattern()
	{
		return (this._dateExpression == null);
	}

	public String getDatePattern()
	{
		return (this._dateExpression == null ? this.dataFormat.getDateFormat() : this._dateExpression.getContent());
	}

	/**
	 * 是否是纯时间格式，而非表达式。
	 * 
	 * @return
	 */
	public boolean isPureTimePattern()
	{
		return (this._timeExpression == null);
	}

	public String getTimePattern()
	{
		return (this._timeExpression == null ? this.dataFormat.getTimeFormat() : this._timeExpression.getContent());
	}

	/**
	 * 是否是纯时间戳格式，而非表达式。
	 * 
	 * @return
	 */
	public boolean isPureTimestampPattern()
	{
		return (this._timestampExpression == null);
	}

	public String getTimestampPattern()
	{
		return (this._timestampExpression == null ? this.dataFormat.getTimestampFormat()
				: this._timestampExpression.getContent());
	}

	/**
	 * 是否是纯数值格式，而非表达式。
	 * 
	 * @return
	 */
	public boolean isPureNumberPattern()
	{
		return (this._numberExpression == null);
	}

	public String getNumberPattern()
	{
		return (this._numberExpression == null ? this.dataFormat.getNumberFormat()
				: this._numberExpression.getContent());
	}

	/**
	 * 是否是纯二进制格式，而非表达式。
	 * 
	 * @return
	 */
	public boolean isPureBinaryPattern()
	{
		return (this._binaryExpression == null);
	}

	public String getBinaryPattern()
	{
		return (this._binaryExpression == null ? this.dataFormat.getBinaryFormat()
				: this._binaryExpression.getContent());
	}

	public DateFormat getDateFormat()
	{
		return _dateFormat;
	}

	public DateFormat getTimeFormat()
	{
		return _timeFormat;
	}

	public DateFormat getTimestampFormat()
	{
		return _timestampFormat;
	}

	public NumberFormat getNumberFormat()
	{
		return _numberFormat;
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
		if (value == null || value.isEmpty())
			return null;

		if (this._dateExpression != null)
		{
			value = this.expressionResolver.extract(this.dataFormat.getDateFormat(), this._dateExpression, value);

			if (value == null || value.isEmpty())
				return null;
		}

		java.util.Date d = this._dateFormat.parse(value);
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
		if (value == null || value.isEmpty())
			return null;

		if (this._timeExpression != null)
		{
			value = this.expressionResolver.extract(this.dataFormat.getTimeFormat(), this._timeExpression, value);

			if (value == null || value.isEmpty())
				return null;
		}

		java.util.Date d = this._timeFormat.parse(value);
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
		if (value == null || value.isEmpty())
			return null;

		if (this._timestampExpression != null)
		{
			value = this.expressionResolver.extract(this.dataFormat.getTimestampFormat(), this._timestampExpression,
					value);

			if (value == null || value.isEmpty())
				return null;
		}

		Timestamp ts = null;

		// 如果是默认格式，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
		if (DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(getTimestampPattern()))
		{
			ts = Timestamp.valueOf(value);
		}
		else
		{
			// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决
			java.util.Date tsdv = this._timeFormat.parse(value);
			ts = new Timestamp(tsdv.getTime());
		}

		return ts;
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
		if (value == null || value.isEmpty())
			return null;

		String binaryPattern = getBinaryPattern();

		if (DataFormat.BINARY_FORMAT_NULL.equalsIgnoreCase(binaryPattern))
			return null;

		if (this._binaryExpression != null)
			value = this.expressionResolver.extract(this.dataFormat.getBinaryFormat(), this._binaryExpression, value);

		byte[] bytes = null;

		if (DataFormat.BINARY_FORMAT_HEX.equalsIgnoreCase(binaryPattern))
		{
			bytes = convertToBytesForHex(value);
		}
		else if (DataFormat.BINARY_FORMAT_BASE64.equalsIgnoreCase(binaryPattern))
		{
			bytes = convertToBytesForBase64(value);
		}
		else
			throw new UnsupportedOperationException("'" + binaryPattern + "' binary format is not supported");

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
		String sv = (value == null ? null : this._dateFormat.format(value));

		if (sv != null && this._dateExpression != null)
			sv = this.expressionResolver.evaluate(this.dataFormat.getDateFormat(), this._dateExpression, sv, "");

		return sv;
	}

	/**
	 * 格式化{@linkplain Time}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatTime(Time value)
	{
		String sv = (value == null ? null : this._timeFormat.format(value));

		if (sv != null && this._timeExpression != null)
			sv = this.expressionResolver.evaluate(this.dataFormat.getTimeFormat(), this._timeExpression, sv, "");

		return sv;
	}

	/**
	 * 格式化{@linkplain Timestamp}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatTimestamp(Timestamp value)
	{
		String sv = null;

		if (value == null)
		{
		}
		// 如果是默认格式且有纳秒值，则直接使用Timestamp.valueOf，这样可以避免丢失纳秒精度
		else if (value.getNanos() > 0 && DataFormat.DEFAULT_TIMESTAMP_FORMAT.equals(getTimestampPattern()))
		{
			sv = value.toString();
		}
		else
		{
			// XXX 这种处理方式会丢失纳秒数据，待以后版本升级至jdk1.8库时采用java.time可解决

			sv = this._timestampFormat.format(value);
		}

		if (sv != null && this._timestampExpression != null)
			sv = this.expressionResolver.evaluate(this.dataFormat.getTimestampFormat(), this._timestampExpression, sv,
					"");

		return sv;
	}

	/**
	 * 格式化{@code int}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatInt(Integer value)
	{
		return formatNumber(value);
	}

	/**
	 * 格式化{@code long}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatLong(Long value)
	{
		return formatNumber(value);
	}

	/**
	 * 格式化{@code float}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatFloat(Float value)
	{
		return formatNumber(value);
	}

	/**
	 * 格式化{@code double}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatDouble(Double value)
	{
		return formatNumber(value);
	}

	/**
	 * 格式化{@code Number}。
	 * 
	 * @param value
	 * @return
	 */
	public String formatNumber(Number value)
	{
		String sv = (value == null ? null : this._numberFormat.format(value));

		if (sv != null && this._numberExpression != null)
			sv = this.expressionResolver.evaluate(this.dataFormat.getNumberFormat(), this._numberExpression, sv, "");

		return sv;
	}

	/**
	 * 格式化字节数组。
	 * 
	 * @param value
	 * @return
	 */
	public String formatBytes(byte[] value)
	{
		String binaryPattern = getBinaryPattern();

		if (value == null || DataFormat.BINARY_FORMAT_NULL.equalsIgnoreCase(binaryPattern))
			return null;

		String sv = null;

		if (DataFormat.BINARY_FORMAT_HEX.equalsIgnoreCase(binaryPattern))
			sv = convertToHex(value);
		else if (DataFormat.BINARY_FORMAT_BASE64.equalsIgnoreCase(binaryPattern))
			sv = convertToBase64(value);
		else
			throw new UnsupportedOperationException("'" + binaryPattern + "' binary format is not supported");

		if (this._binaryExpression != null)
			sv = this.expressionResolver.evaluate(this.dataFormat.getBinaryFormat(), this._binaryExpression, sv, "");

		return sv;
	}

	@Override
	protected BigDecimal doParseBigDecimal(String value) throws NumberParserException
	{
		if (this._numberExpression != null)
		{
			value = this.expressionResolver.extract(this.dataFormat.getNumberFormat(), this._numberExpression, value);

			if (value == null || value.isEmpty())
				return null;
		}

		try
		{
			return (BigDecimal) this._numberFormat.parse(value);
		}
		catch (ParseException e)
		{
			throw new NumberParserException(e);
		}
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

		return Base64.getEncoder().encodeToString(bytes);
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

	/**
	 * 将指定格式字符串包裹为表达式。
	 * 
	 * @param pattern
	 * @return
	 */
	public static String wrapToExpression(String pattern)
	{
		return EXP_START_IDENTIFIER + pattern + EXP_END_IDENTIFIER;
	}

	public static class DataFormatExpressionResolver extends ExpressionResolver
	{
		public DataFormatExpressionResolver()
		{
			super();
			super.setStartIdentifier(EXP_START_IDENTIFIER);
			super.setEndIdentifier(EXP_END_IDENTIFIER);
		}
	}
}
