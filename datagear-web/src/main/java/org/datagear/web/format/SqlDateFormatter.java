/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
			java.util.Date candidate = parseByPatterns(text, PATTERN_YEAR_MONTH_DAY, PATTERN_YEAR_MONTH, PATTERN_YEAR);

			if (candidate != null)
				return new Date(candidate.getTime());
			else
				throw new ParseException(text, 0);
		}
	}

	@Override
	public String print(Date object, Locale locale)
	{
		// XXX 不能直接使用此处代码，因为它只能处理4位年份的日期，导致精度丢失
		// return object.toString();

		return formatByPattern(object, PATTERN_YEAR_MONTH_DAY);
	}

	@Override
	public String getParsePatternDesc(Locale locale)
	{
		return PATTERN_YEAR_MONTH_DAY;
	}
}
