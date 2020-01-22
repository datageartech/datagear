/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.AbstractRenderContext;

/**
 * {@linkplain HtmlRenderContext} JS脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlRenderContextScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public HtmlRenderContextScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlRenderContext}的JS脚本对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]=
	 * {
	 * 	webContext : {...},
	 * 	attributes : {...}
	 * };
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void write(Writer out, HtmlRenderContext renderContext, String varName) throws IOException
	{
		JsonHtmlRenderContext jsonRenderContext = new JsonHtmlRenderContext(renderContext);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 将{@linkplain HtmlRenderContext}的JS脚本对象写入输出流，并且忽略{@linkplain RenderContext#getAttributes()}属性。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]=
	 * {
	 * 	webContext : {...}
	 * };
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void writeNoAttributes(Writer out, HtmlRenderContext renderContext, String varName) throws IOException
	{
		JsonNoAttributesHtmlRenderContext jsonRenderContext = new JsonNoAttributesHtmlRenderContext(renderContext);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 将{@linkplain HtmlRenderContext}的JS脚本对象写入输出流，并且仅输出{@linkplain RenderContext#getAttributes()}属性。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]=
	 * {
	 * 	attributes : {...}
	 * };
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void writeOnlyAttributes(Writer out, HtmlRenderContext renderContext, String varName) throws IOException
	{
		JsonOnlyAttributesHtmlRenderContext jsonRenderContext = new JsonOnlyAttributesHtmlRenderContext(renderContext);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 可输出为JSON的{@linkplain HtmlRenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonHtmlRenderContext extends DefaultHtmlRenderContext
	{
		@SuppressWarnings("unchecked")
		public JsonHtmlRenderContext(HtmlRenderContext renderContext)
		{
			super();
			setWebContext(renderContext.getWebContext());
			setAttributes((Map<String, Object>) renderContext.getAttributes());
		}
	}

	/**
	 * 可输出为JSON且{@linkplain HtmlRenderContext#getAttributes()}为空的{@linkplain HtmlRenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonNoAttributesHtmlRenderContext extends AbstractRenderContext implements HtmlRenderContext
	{
		private WebContext webContext;

		public JsonNoAttributesHtmlRenderContext(HtmlRenderContext renderContext)
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
	 * 可输出为JSON且仅带有{@linkplain RenderContext#getAttributes()}的{@linkplain HtmlRenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JsonOnlyAttributesHtmlRenderContext extends AbstractRenderContext implements HtmlRenderContext
	{
		public JsonOnlyAttributesHtmlRenderContext(RenderContext renderContext)
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
}
