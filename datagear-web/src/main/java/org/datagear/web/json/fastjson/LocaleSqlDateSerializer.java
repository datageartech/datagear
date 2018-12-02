/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.datagear.web.format.SqlDateFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.sql.Date}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleSqlDateSerializer implements ObjectSerializer
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
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String str = null;

		if (object != null)
			str = this.sqlDateFormatter.print((java.sql.Date) object, LocaleContextHolder.getLocale());

		serializer.write(str);
	}
}
