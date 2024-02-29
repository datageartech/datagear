/*
 * Copyright 2018-2024 datagear.tech
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

import org.datagear.util.StringUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * 转义HTML的字符串{@linkplain JsonSerializer}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class EscapeHtmlStringSerializer extends JsonSerializer<String>
{
	public EscapeHtmlStringSerializer()
	{
		super();
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException
	{
		String str = (value == null ? null : StringUtil.escapeHtml(value));
		
		if(str == null)
			gen.writeNull();
		else
			gen.writeString(str);
	}
}
