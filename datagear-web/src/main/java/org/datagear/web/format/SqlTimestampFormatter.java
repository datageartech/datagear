/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Locale;

/**
 * {@linkplain java.sql.Timestamp}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTimestampFormatter extends AbstractDateFormatter<Timestamp>
{
	public static final String PATTERN = "yyyy-MM-dd hh:mm:ss[.fff...]";

	public SqlTimestampFormatter()
	{
		super();
	}

	@Override
	public Timestamp parse(String text, Locale locale) throws ParseException
	{
		try
		{
			return Timestamp.valueOf(text);
		}
		catch (Exception e)
		{
			throw new ParseException(text, 0);
		}
	}

	@Override
	public String print(Timestamp object, Locale locale)
	{
		return object.toString();
	}

	@Override
	public String getParsePattern(Locale locale)
	{
		return PATTERN;
	}
}
