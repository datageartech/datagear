/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * 抽象日期相关的{@linkplain Formatter}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDateFormatter<T extends Date> implements Formatter<T>
{
	private int printStyle = DateFormat.MEDIUM;

	public AbstractDateFormatter()
	{
		super();
	}

	public int getPrintStyle()
	{
		return printStyle;
	}

	public void setPrintStyle(int printStyle)
	{
		this.printStyle = printStyle;
	}

	/**
	 * 获取可解析的格式。
	 * 
	 * @param locale
	 * @return
	 */
	public String getParsePattern(Locale locale)
	{
		DateFormat dateFormat = getParseDateFormat(locale);

		if (dateFormat instanceof SimpleDateFormat)
			return ((SimpleDateFormat) dateFormat).toPattern();
		else
			return "";
	}

	@Override
	public String print(T object, Locale locale)
	{
		return getParseDateFormat(locale).format(object);
	}

	/**
	 * 将文本解析为{@linkplain Date}。
	 * 
	 * @param text
	 * @param locale
	 * @return
	 * @throws ParseException
	 */
	protected Date parseToDate(String text, Locale locale) throws ParseException
	{
		DateFormat dateFormat = getParseDateFormat(locale);

		try
		{
			return dateFormat.parse(text);
		}
		catch (Exception e)
		{
			throw new ParseException(text, 0);
		}
	}

	/**
	 * 获取解析类。
	 * 
	 * @param locale
	 * @return
	 */
	protected abstract DateFormat getParseDateFormat(Locale locale);
}
