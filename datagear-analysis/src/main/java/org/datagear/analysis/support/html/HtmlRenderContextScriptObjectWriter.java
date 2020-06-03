/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.DefaultRenderContext;

/**
 * HTML {@linkplain RenderContext} JS脚本对象输出流。
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
	 * 将{@linkplain RenderContext}的JS脚本对象写入输出流。
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
	public void write(Writer out, RenderContext renderContext, String varName) throws IOException
	{
		write(out, renderContext, varName, null);
	}

	/**
	 * 将{@linkplain RenderContext}的JS脚本对象写入输出流，并忽略某些属性。
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
	 * @param ignoreAttrs
	 *            忽略属性，允许为{@code null}
	 * @throws IOException
	 */
	public void write(Writer out, RenderContext renderContext, String varName, Collection<String> ignoreAttrs)
			throws IOException
	{
		JsonRenderContext jsonRenderContext = new JsonRenderContext(renderContext, ignoreAttrs);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 将{@linkplain RenderContext}的JS脚本对象写入输出流，并且忽略{@linkplain RenderContext#getAttributes()}属性。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]= {};
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void writeNoAttributes(Writer out, RenderContext renderContext, String varName) throws IOException
	{
		JsonNoAttributesRenderContext jsonRenderContext = new JsonNoAttributesRenderContext(renderContext);

		out.write("var " + varName + "=");
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 可输出为JSON的{@linkplain RenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonRenderContext extends DefaultRenderContext
	{
		public JsonRenderContext(RenderContext renderContext)
		{
			this(renderContext, null);
		}

		@SuppressWarnings("unchecked")
		public JsonRenderContext(RenderContext renderContext, Collection<String> ignoreAttrs)
		{
			super();
			setAttributes((Map<String, Object>) renderContext.getAttributes());

			if (ignoreAttrs != null)
			{
				for (String attr : ignoreAttrs)
					removeAttribute(attr);
			}
		}
	}

	/**
	 * 可输出为JSON且{@linkplain RenderContext#getAttributes()}为空的{@linkplain RenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonNoAttributesRenderContext extends DefaultRenderContext
	{
		public JsonNoAttributesRenderContext(RenderContext renderContext)
		{
			super();
			super.setAttributes(null);
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
	}

	/**
	 * 可输出为JSON且仅带有{@linkplain RenderContext#getAttributes()}的{@linkplain RenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonOnlyAttributesRenderContext extends DefaultRenderContext
	{
		public JsonOnlyAttributesRenderContext(RenderContext renderContext)
		{
			super(renderContext.getAttributes());
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
	}
}
