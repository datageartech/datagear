/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.jackson;

import java.io.IOException;
import java.sql.Date;

import org.datagear.web.format.SqlDateFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Date}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlDateSerializer extends JsonSerializer<Date>
{
	private SqlDateFormatter sqlDateFormatter;

	public LocaleSqlDateSerializer()
	{
		super();
	}

	public LocaleSqlDateSerializer(SqlDateFormatter sqlDateFormatter)
	{
		super();
		this.sqlDateFormatter = sqlDateFormatter;
	}

	public SqlDateFormatter getSqlDateFormatter()
	{
		return sqlDateFormatter;
	}

	public void setSqlDateFormatter(SqlDateFormatter sqlDateFormatter)
	{
		this.sqlDateFormatter = sqlDateFormatter;
	}

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = null;

		if (value != null)
			str = this.sqlDateFormatter.print(value, LocaleContextHolder.getLocale());

		serializers.defaultSerializeValue(str, gen);
	}
}
