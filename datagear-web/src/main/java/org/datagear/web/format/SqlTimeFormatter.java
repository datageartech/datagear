/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.sql.Time;
import java.text.ParseException;
import java.util.Locale;

/**
 * {@linkplain java.sql.Time}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTimeFormatter extends AbstractDateFormatter<Time>
{
	public static final String PATTERN = "hh:mm:ss";

	public SqlTimeFormatter()
	{
		super();
	}

	@Override
	public Time parse(String text, Locale locale) throws ParseException
	{
		try
		{
			return Time.valueOf(text);
		}
		catch (Exception e)
		{
			throw new ParseException(text, 0);
		}
	}

	@Override
	public String print(Time object, Locale locale)
	{
		return object.toString();
	}

	@Override
	public String getParsePattern(Locale locale)
	{
		return PATTERN;
	}
}
