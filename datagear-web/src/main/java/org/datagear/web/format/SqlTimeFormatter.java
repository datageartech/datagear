/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
