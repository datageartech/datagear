/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.sql.Date;
import java.text.ParseException;
import java.util.Locale;

/**
 * {@linkplain java.sql.Date}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlDateFormatter extends AbstractDateFormatter<Date>
{
	public static final String PATTERN = "yyyy-MM-dd";

	public SqlDateFormatter()
	{
		super();
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException
	{
		try
		{
			return Date.valueOf(text);
		}
		catch (Exception e)
		{
			throw new ParseException(text, 0);
		}
	}

	@Override
	public String print(Date object, Locale locale)
	{
		return object.toString();
	}

	@Override
	public String getParsePattern(Locale locale)
	{
		return PATTERN;
	}
}
