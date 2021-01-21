/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.format;

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
	public static final String PATTERN_YEAR = "yyyy";

	public static final String PATTERN_YEAR_MONTH = "yyyy-MM";

	public static final String PATTERN_YEAR_MONTH_DAY = "yyyy-MM-dd";

	public static final String PATTERN_YEAR_MONTH_DAY_HOUR = "yyyy-MM-dd HH";

	public static final String PATTERN_YEAR_MONTH_DAY_HOUR_MIN = "yyyy-MM-dd HH:mm";

	public static final String PATTERN_YEAR_MONTH_DAY_HOUR_MIN_SEC = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 获取解析格式描述。
	 * 
	 * @param locale
	 * @return
	 */
	public abstract String getParsePatternDesc(Locale locale);

	/**
	 * 将日期格式化。
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	protected String formatByPattern(Date date, String pattern)
	{
		return getDateFormatForPattern(pattern).format(date);
	}

	/**
	 * 获取指定格式的{@linkplain SimpleDateFormat}。
	 * 
	 * @param pattern
	 * @return
	 */
	protected SimpleDateFormat getDateFormatForPattern(String pattern)
	{
		return new SimpleDateFormat(pattern);
	}

	/**
	 * 使用特定格式解析。
	 * <p>
	 * 注意：对于{@code patterns}参数，越精确的的格式应该越靠前，因为它们最先被使用，保证不会丢失精度。
	 * </p>
	 * <p>
	 * 如果无法解析，将返回{@code null}。
	 * </p>
	 * 
	 * @param text
	 * @param patterns
	 * @return
	 */
	protected Date parseByPatterns(String text, String... patterns)
	{
		for (int i = 0; i < patterns.length; i++)
		{
			String pattern = patterns[i];

			try
			{
				return new SimpleDateFormat(pattern).parse(text);
			}
			catch (ParseException e)
			{
			}
		}

		return null;
	}
}
