/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
