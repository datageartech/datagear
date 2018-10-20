/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.datagear.web.format.DateFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 基于Spring的{@linkplain LocaleContextHolder}的{@linkplain java.util.Date}序列化器。
 * 
 * @author datagear@163.com
 *
 */
public class LocaleDateSerializer implements ObjectSerializer
{
	private DateFormatter dateFormatter = new DateFormatter();

	public LocaleDateSerializer()
	{
		super();
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
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String str = null;

		if (object != null)
			str = this.dateFormatter.print((java.util.Date) object, LocaleContextHolder.getLocale());

		serializer.write(str);
	}
}
