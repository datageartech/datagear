/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.format;

import java.text.ParseException;
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
	public DateFormatter()
	{
		super();
	}

	@Override
	public String print(Date object, Locale locale)
	{
		// XXX 统一采用SqlDateFormatter.print(Date, Locale)的算法
		return formatByPattern(object, PATTERN_YEAR_MONTH_DAY_HOUR_MIN_SEC);
	}

	@Override
	public String getParsePatternDesc(Locale locale)
	{
		return PATTERN_YEAR_MONTH_DAY_HOUR_MIN_SEC;
	}

	@Override
	public Date parse(String text, Locale locale) throws ParseException
	{
		Date candidate = parseByPatterns(text, PATTERN_YEAR_MONTH_DAY_HOUR_MIN_SEC, PATTERN_YEAR_MONTH_DAY_HOUR_MIN,
				PATTERN_YEAR_MONTH_DAY_HOUR, PATTERN_YEAR_MONTH_DAY, PATTERN_YEAR_MONTH, PATTERN_YEAR);

		if (candidate != null)
			return candidate;
		else
			throw new ParseException(text, 0);
	}
}
