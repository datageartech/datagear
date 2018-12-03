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
	public static final String PATTERN_HOUR = "HH";

	public static final String PATTERN_HOUR_MIN = "HH:mm";

	public static final String PATTERN_HOUR_MIN_SEC = "HH:mm:ss";

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
			java.util.Date candidate = parseByPatterns(text, PATTERN_HOUR_MIN_SEC, PATTERN_HOUR_MIN, PATTERN_HOUR);

			if (candidate != null)
				return new Time(candidate.getTime());
			else
				throw new ParseException(text, 0);
		}
	}

	@Override
	public String print(Time object, Locale locale)
	{
		// XXX 统一采用SqlDateFormatter.print(Date, Locale)的算法
		return formatByPattern(object, PATTERN_HOUR_MIN_SEC);
	}

	@Override
	public String getParsePatternDesc(Locale locale)
	{
		return PATTERN_HOUR_MIN_SEC;
	}
}
