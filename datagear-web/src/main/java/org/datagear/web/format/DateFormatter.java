/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.format;

import java.text.DateFormat;
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
	public Date parse(String text, Locale locale) throws ParseException
	{
		return parseToDate(text, locale);
	}

	@Override
	protected DateFormat getParseDateFormat(Locale locale)
	{
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);

		return dateFormat;
	}
}
