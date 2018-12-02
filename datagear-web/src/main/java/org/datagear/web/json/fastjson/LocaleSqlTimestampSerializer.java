/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.datagear.web.format.SqlTimestampFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Timestamp}
 * 序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlTimestampSerializer implements ObjectSerializer
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
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String str = null;

		if (object != null)
			str = this.sqlTimestampFormatter.print((java.sql.Timestamp) object, LocaleContextHolder.getLocale());

		serializer.write(str);
	}
}
