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

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.Formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 基于{@linkplain Formatter}的字符串序列化器。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class FormatterSerializer<T> extends JsonSerializer<T>
{
	private Formatter<T> formatter;

	public FormatterSerializer(Formatter<T> formatter)
	{
		super();
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

	@Override
	public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = null;

		if (value != null)
			str = this.formatter.print(value, LocaleContextHolder.getLocale());

		serializers.defaultSerializeValue(str, gen);
	}
}
