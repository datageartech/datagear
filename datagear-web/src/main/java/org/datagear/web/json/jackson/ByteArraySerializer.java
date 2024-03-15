/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.web.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 字节数组{@linkplain JsonSerializer}。
 * <p>
 * 此类输出原始字节数组。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ByteArraySerializer extends JsonSerializer<byte[]>
{
	public ByteArraySerializer()
	{
		super();
	}

	@Override
	public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		if (value == null)
			serializers.defaultSerializeValue(null, gen);
		else
		{
			gen.writeStartArray(value);

			for (int i = 0, len = value.length; i < len; ++i)
			{
				gen.writeNumber(value[i]);
			}

			gen.writeEndArray();
		}
	}
}
