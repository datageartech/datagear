/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.web.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * {@linkplain Long}型作为字符串输出的{@linkplain JsonSerializer}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class LongToStringSerializer extends JsonSerializer<Long>
{
	public LongToStringSerializer()
	{
		super();
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = (value == null ? null : value.toString());
		serializers.defaultSerializeValue(str, gen);
	}
}
