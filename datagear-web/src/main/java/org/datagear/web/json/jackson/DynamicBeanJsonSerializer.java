/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.datagear.model.support.DynamicBean;

/**
 * {@linkplain DynamicBean}的{@linkplain JsonSerializer}。
 * 
 * @author datagear@163.com
 *
 */
public class DynamicBeanJsonSerializer extends JsonSerializer<DynamicBean>
{
	public DynamicBeanJsonSerializer()
	{
		super();
	}

	@Override
	public void serialize(DynamicBean value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException
	{
		provider.defaultSerializeValue(value, jgen);

		/**
		 * DynamicBean未继承Map时的序列化方案
		 */
		// if (value == null)
		// provider.defaultSerializeNull(jgen);
		// else
		// {
		// Map<String, ?> properties = value.getPropertyValues();
		//
		// provider.defaultSerializeValue(properties, jgen);
		// }
	}
}
