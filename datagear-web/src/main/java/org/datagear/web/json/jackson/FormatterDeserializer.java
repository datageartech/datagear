/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
