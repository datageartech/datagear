/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

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
		module.addSerializer(RefHtmlRenderContext.class, new RefObjectSerializer());
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

	protected static class RefHtmlRenderContext implements HtmlRenderContext, JsonRefObject
	{
		private String refName;

		public RefHtmlRenderContext(String refName)
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
		public WebContext getWebContext()
		{
			return null;
		}

		@Override
		public Writer getWriter()
		{
			return null;
		}

		@Override
		public int nextSequence()
		{
			return 0;
		}
	}

	protected static class RefHtmlChartPlugin extends HtmlChartPlugin<HtmlRenderContext> implements JsonRefObject
	{
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
