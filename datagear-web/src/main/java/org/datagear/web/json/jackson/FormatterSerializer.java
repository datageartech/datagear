/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
