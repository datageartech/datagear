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

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.JsonSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 抽象HTML脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractHtmlScriptObjectWriter
{
	private ObjectMapper objectMapper;

	/** 换行符 */
	private String newLine = HtmlChartPlugin.HTML_NEW_LINE;

	public AbstractHtmlScriptObjectWriter()
	{
		super();

		this.objectMapper = JsonSupport.create();
		JsonSupport.setWriteJsonFeatures(this.objectMapper);
		JsonSupport.disableAutoCloseTargetFeature(objectMapper);

		SimpleModule module = new SimpleModule(RefObjectSerializer.class.getSimpleName());
		module.addSerializer(RefRenderContext.class, new RefObjectSerializer());
		module.addSerializer(RefHtmlChartPlugin.class, new RefObjectSerializer());
		this.objectMapper.registerModule(module);
	}

	public ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	/**
	 * 写JSON对象。
	 * 
	 * @param out
	 * @param object
	 * @throws IOException
	 */
	protected void writeJsonObject(Writer out, Object object) throws IOException
	{
		this.objectMapper.writeValue(out, object);
	}

	/**
	 * 写换行符。
	 * 
	 * @param out
	 * @throws IOException
	 */
	protected void writeNewLine(Writer out) throws IOException
	{
		out.write(this.newLine);
	}

	/**
	 * JSON引用名对象。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static interface JsonRefObject
	{
		String getRefName();
	}

	protected static class RefObjectSerializer extends JsonSerializer<JsonRefObject>
	{
		public RefObjectSerializer()
		{
			super();
		}

		@Override
		public void serialize(JsonRefObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException
		{
			if (value == null)
				serializers.defaultSerializeNull(gen);
			else
			{
				String refName = value.getRefName();
				gen.writeRawValue(refName);
			}
		}
	}

	protected static class RefRenderContext implements RenderContext, JsonRefObject, Serializable
	{
		private static final long serialVersionUID = 1L;

		private String refName;

		public RefRenderContext(String refName)
		{
			super();
			this.refName = refName;
		}

		@Override
		public String getRefName()
		{
			return refName;
		}

		@Override
		public <T> T getAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAttribute(String name, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T removeAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, ?> getAttributes()
		{
			return null;
		}

		@Override
		public void putAttributes(Map<String, ?> attrs)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAttributes(RenderContext renderContext)
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class RefHtmlChartPlugin extends HtmlChartPlugin implements JsonRefObject
	{
		private static final long serialVersionUID = 1L;

		private String refName;

		public RefHtmlChartPlugin(String refName)
		{
			super();
			this.refName = refName;
		}

		@Override
		public String getRefName()
		{
			return refName;
		}
	}
}
