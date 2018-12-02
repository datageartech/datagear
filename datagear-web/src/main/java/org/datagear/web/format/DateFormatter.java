/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * {@linkplain java.util.Date}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class DateFormatter extends AbstractDateFormatter<Date>
{
	private int printStyle = DateFormat.MEDIUM;

	private String candidateParsePattern = "yyyy-MM-dd";

	public DateFormatter()
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

	public String getCandidateParsePattern()
	{
		return candidateParsePattern;
	}

	public void setCandidateParsePattern(String candidateParsePattern)
	{
		this.candidateParsePattern = candidateParsePattern;
	}

	@Override
	public String print(Date object, Locale locale)
	{
		return getParseDateFormat(locale).format(object);
	}

	@Override
	public String getParsePattern(Locale locale)
	{
		DateFormat dateFormat = getParseDateFormat(locale);

		if (dateFormat instanceof SimpleDateFormat)
			return ((SimpleDateFormat) dateFormat).toPattern();
		else
			return candidateParsePattern;
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException
	{
		try
		{
			return parseToDate(text, locale);
		}
		catch (ParseException e)
		{
			return new SimpleDateFormat(this.candidateParsePattern).parse(text);
		}
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

	protected DateFormat getParseDateFormat(Locale locale)
	{
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);

		return dateFormat;
	}
}
