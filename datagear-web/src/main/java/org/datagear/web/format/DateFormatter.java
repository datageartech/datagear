/*
 * Copyright 2018-2024 datagear.tech
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
