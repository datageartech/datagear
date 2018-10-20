/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.sql.Date;
import java.text.DateFormat;
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
		java.util.Date date = parseToDate(text, locale);

		return new Date(date.getTime());
	}

	@Override
	protected DateFormat getParseDateFormat(Locale locale)
	{
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		return dateFormat;
	}
}
