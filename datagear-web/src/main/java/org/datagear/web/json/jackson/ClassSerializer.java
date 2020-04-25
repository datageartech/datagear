/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * {@linkplain Class}的{@linkplain JsonSerializer}。
 * 
 * @author datagear@163.com
 *
 */
@SuppressWarnings("rawtypes")
public class ClassSerializer extends JsonSerializer<Class>
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
	public void serialize(Class value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String className = "";
		serializers.defaultSerializeValue(className, gen);
	}
}
