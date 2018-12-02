/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.datagear.web.format.SqlTimeFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Time}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlTimeSerializer implements ObjectSerializer
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
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String str = null;

		if (object != null)
			str = this.sqlTimeFormatter.print((java.sql.Time) object, LocaleContextHolder.getLocale());

		serializer.write(str);
	}
}
