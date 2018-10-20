/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * {@linkplain Class}的{@linkplain ObjectSerializer}。
 * 
 * @author datagear@163.com
 *
 */
public class ClassSerializer implements ObjectSerializer
{
	private boolean simple;

	public ClassSerializer()
	{
		super();
	}

	public ClassSerializer(boolean simple)
	{
		super();
		this.simple = simple;
	}

	public boolean isSimple()
	{
		return simple;
	}

	public void setSimple(boolean simple)
	{
		this.simple = simple;
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String className = "";

		if (object != null)
			className = (this.simple ? ((Class<?>) object).getSimpleName() : ((Class<?>) object).getName());

		serializer.write(className);
	}
}
