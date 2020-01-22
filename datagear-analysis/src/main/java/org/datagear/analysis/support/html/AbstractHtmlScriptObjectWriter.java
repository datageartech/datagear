/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 抽象HTML脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractHtmlScriptObjectWriter
{
	private static final SerializerFeature[] DEFAULT_SERIALIZER_FEATURES = new SerializerFeature[] {
			SerializerFeature.QuoteFieldNames, SerializerFeature.WriteEnumUsingName,
			SerializerFeature.DisableCircularReferenceDetect };

	private SerializerFeature[] serializerFeatures = DEFAULT_SERIALIZER_FEATURES;

	private SerializeConfig serializeConfig = new SerializeConfig();

	/** 换行符 */
	private String newLine = HtmlChartPlugin.HTML_NEW_LINE;

	public AbstractHtmlScriptObjectWriter()
	{
		super();
		initSerializeConfig(this.serializeConfig);
	}

	public SerializerFeature[] getSerializerFeatures()
	{
		return serializerFeatures;
	}

	public void setSerializerFeatures(SerializerFeature[] serializerFeatures)
	{
		this.serializerFeatures = serializerFeatures;
	}

	protected SerializeConfig getSerializeConfig()
	{
		return serializeConfig;
	}

	public void setSerializeConfig(SerializeConfig serializeConfig)
	{
		this.serializeConfig = serializeConfig;
	}

	public String getNewLine()
	{
		return newLine;
	}

	public void setNewLine(String newLine)
	{
		this.newLine = newLine;
	}

	protected void initSerializeConfig(SerializeConfig serializeConfig)
	{
		RefObjectSerializer refHtmlRenderContextSerializer = new RefObjectSerializer();
		serializeConfig.put(RefHtmlRenderContext.class, refHtmlRenderContextSerializer);
		serializeConfig.put(RefHtmlChartPlugin.class, refHtmlRenderContextSerializer);
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
		SerializeWriter serializeWriter = new SerializeWriter(out, this.serializerFeatures);
		JSONSerializer serializer = new JSONSerializer(serializeWriter, this.serializeConfig);

		try
		{
			serializer.write(object);
		}
		finally
		{
			serializeWriter.flush();
		}
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

	protected static class RefObjectSerializer implements ObjectSerializer
	{
		@Override
		public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
				throws IOException
		{
			String refName = null;

			if (object != null)
			{
				JsonRefObject jsonRefObject = (JsonRefObject) object;
				refName = jsonRefObject.getRefName();
			}

			serializer.getWriter().append(refName);
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
