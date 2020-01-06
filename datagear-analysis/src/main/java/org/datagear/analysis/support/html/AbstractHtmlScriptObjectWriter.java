/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.AbstractRenderContext;

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

	protected void initSerializeConfig(SerializeConfig serializeConfig)
	{
		RefRenderContextSerializer refRenderContextSerializer = new RefRenderContextSerializer();

		serializeConfig.put(RefHtmlRenderContext.class, refRenderContextSerializer);
	}

	/**
	 * 将{@linkplain RenderContext}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param renderContext
	 * @throws IOException
	 */
	public void writeRenderContext(Writer out, RenderContext renderContext) throws IOException
	{
		writeRenderContext(out, renderContext, false);
	}

	/**
	 * 将{@linkplain RenderContext}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param renderContext
	 * @param onlyAttributes
	 * @throws IOException
	 */
	public void writeRenderContext(Writer out, RenderContext renderContext, boolean onlyAttributes) throws IOException
	{
		if (onlyAttributes)
			renderContext = new AttributesHtmlRenderContext(renderContext);

		writeScriptObject(out, renderContext);
	}

	/**
	 * 写脚本对象。
	 * 
	 * @param out
	 * @param object
	 * @throws IOException
	 */
	protected void writeScriptObject(Writer out, Object object) throws IOException
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
	 * {@linkplain HtmlRenderContext#getAttributes()}为空的{@linkplain HtmlRenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class NoAttributesHtmlRenderContext extends AbstractRenderContext implements HtmlRenderContext
	{
		private WebContext webContext;

		public NoAttributesHtmlRenderContext(HtmlRenderContext renderContext)
		{
			super();
			super.setAttributes(null);
			this.webContext = renderContext.getWebContext();
		}

		@Override
		public WebContext getWebContext()
		{
			return webContext;
		}

		public void setWebContext(WebContext webContext)
		{
			this.webContext = webContext;
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

	/**
	 * 仅带有{@linkplain RenderContext#getAttributes()}的{@linkplain HtmlRenderContext}。
	 * <p>
	 * 此类仅用于脚本输出。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class AttributesHtmlRenderContext extends AbstractRenderContext implements HtmlRenderContext
	{
		public AttributesHtmlRenderContext(RenderContext renderContext)
		{
			super(renderContext.getAttributes());
		}

		@Override
		public WebContext getWebContext()
		{
			return null;
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

	/**
	 * 引用名{@linkplain HtmlRenderContext}。
	 * <p>
	 * 此类仅用于脚本输出。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class RefHtmlRenderContext implements HtmlRenderContext
	{
		private String refName;

		public RefHtmlRenderContext()
		{
			super();
		}

		public RefHtmlRenderContext(String refName)
		{
			super();
			this.refName = refName;
		}

		public String getRefName()
		{
			return refName;
		}

		public void setRefName(String refName)
		{
			this.refName = refName;
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

	protected static class RefRenderContextSerializer implements ObjectSerializer
	{
		@Override
		public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
				throws IOException
		{
			String refName = null;

			if (object != null)
			{
				RefHtmlRenderContext refHtmlRenderContext = (RefHtmlRenderContext) object;
				refName = refHtmlRenderContext.getRefName();
			}

			serializer.getWriter().append(refName);
		}
	}
}
