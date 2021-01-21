/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.json.jackson;

import java.io.IOException;
import java.sql.Time;

import org.datagear.web.format.SqlTimeFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Time}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlTimeSerializer extends JsonSerializer<Time>
{
	private SqlTimeFormatter sqlTimeFormatter;

	public LocaleSqlTimeSerializer()
	{
		super();
	}

	public LocaleSqlTimeSerializer(SqlTimeFormatter sqlTimeFormatter)
	{
		super();
		this.sqlTimeFormatter = sqlTimeFormatter;
	}

	public SqlTimeFormatter getSqlTimeFormatter()
	{
		return sqlTimeFormatter;
	}

	public void setSqlTimeFormatter(SqlTimeFormatter sqlTimeFormatter)
	{
		this.sqlTimeFormatter = sqlTimeFormatter;
	}

	@Override
	public void serialize(Time value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = null;

		if (value != null)
			str = this.sqlTimeFormatter.print(value, LocaleContextHolder.getLocale());

		serializers.defaultSerializeValue(str, gen);
	}
}
