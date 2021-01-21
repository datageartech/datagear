/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.json.jackson;

import java.io.IOException;
import java.sql.Timestamp;

import org.datagear.web.format.SqlTimestampFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Timestamp}
 * 序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlTimestampSerializer extends JsonSerializer<Timestamp>
{
	private SqlTimestampFormatter sqlTimestampFormatter;

	public LocaleSqlTimestampSerializer()
	{
		super();
	}

	public LocaleSqlTimestampSerializer(SqlTimestampFormatter sqlTimestampFormatter)
	{
		super();
		this.sqlTimestampFormatter = sqlTimestampFormatter;
	}

	public SqlTimestampFormatter getSqlTimestampFormatter()
	{
		return sqlTimestampFormatter;
	}

	public void setSqlTimestampFormatter(SqlTimestampFormatter sqlTimestampFormatter)
	{
		this.sqlTimestampFormatter = sqlTimestampFormatter;
	}

	@Override
	public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = null;

		if (value != null)
			str = this.sqlTimestampFormatter.print(value, LocaleContextHolder.getLocale());

		serializers.defaultSerializeValue(str, gen);
	}
}
