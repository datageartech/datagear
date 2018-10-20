/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * {@linkplain java.sql.Time}格式类。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTimeFormatter extends AbstractDateFormatter<Time>
{
	public SqlTimeFormatter()
	{
		super();
	}

	@Override
	public Time parse(String text, Locale locale) throws ParseException
	{
		Date date = parseToDate(text, locale);

		return new Time(date.getTime());
	}

	@Override
	protected DateFormat getParseDateFormat(Locale locale)
	{
		DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);

		return dateFormat;
	}
}
