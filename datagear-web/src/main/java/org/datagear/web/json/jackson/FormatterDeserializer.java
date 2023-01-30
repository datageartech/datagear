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

package org.datagear.web.json.jackson;

import java.io.IOException;

import org.datagear.util.StringUtil;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.Formatter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

/**
 * 基于{@linkplain Formatter}的字符串反序列化器。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class FormatterDeserializer<T> extends StdScalarDeserializer<T>
{
	private static final long serialVersionUID = 1L;

	private Formatter<T> formatter;

	public FormatterDeserializer(Class<?> vc, Formatter<T> formatter)
	{
		super(vc);
		this.formatter = formatter;
	}

	public Formatter<T> getFormatter()
	{
		return formatter;
	}

	public void setFormatter(Formatter<T> formatter)
	{
		this.formatter = formatter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException
	{
		String str = p.getText();

		if (StringUtil.isEmpty(str))
			return null;

		try
		{
			return this.formatter.parse(str, LocaleContextHolder.getLocale());
		}
		catch (java.text.ParseException e)
		{
			return (T) ctxt.handleWeirdStringValue(_valueClass, str, "invalid string");
		}
	}
}
