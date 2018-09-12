/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.format;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * {@linkplain java.sql.Timestamp}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTimestampFormatter extends AbstractDateFormatter<Timestamp>
{
	public SqlTimestampFormatter()
	{
		super();
	}

	@Override
	public Timestamp parse(String text, Locale locale) throws ParseException
	{
		Date date = parseToDate(text, locale);

		return new Timestamp(date.getTime());
	}

	@Override
	protected DateFormat getParseDateFormat(Locale locale)
	{
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);

		return dateFormat;
	}
}
