/*
 * Copyright 2018-present datagear.tech
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
