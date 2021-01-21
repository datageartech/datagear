/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.json.jackson;

import java.io.IOException;
import java.util.Date;

import org.datagear.web.format.DateFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.util.Date}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleDateSerializer extends JsonSerializer<Date>
{
	private DateFormatter dateFormatter;

	public LocaleDateSerializer()
	{
		super();
	}

	public LocaleDateSerializer(DateFormatter dateFormatter)
	{
		super();
		this.dateFormatter = dateFormatter;
	}

	public DateFormatter getDateFormatter()
	{
		return dateFormatter;
	}

	public void setDateFormatter(DateFormatter dateFormatter)
	{
		this.dateFormatter = dateFormatter;
	}

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = null;

		if (value != null)
			str = this.dateFormatter.print(value, LocaleContextHolder.getLocale());

		serializers.defaultSerializeValue(str, gen);
	}
}
