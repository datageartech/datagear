/*
 * Copyright 2018-2023 datagear.tech
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
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import org.datagear.analysis.DefaultRenderContext;
import org.datagear.analysis.RenderContext;

/**
 * HTML {@linkplain RenderContext} JS脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlRenderContextScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public static final HtmlRenderContextScriptObjectWriter INSTANCE = new HtmlRenderContextScriptObjectWriter();

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
	 * </pre>
	 * </code>
	 * <p>
	 * 注意：只会写入{@linkplain RenderContext#getAttributes()}。
	 * </p>
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
	 * </pre>
	 * </code>
	 * <p>
	 * 注意：只会写入{@linkplain RenderContext#getAttributes()}。
	 * </p>
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
		RenderContextJson renderContextJson = new RenderContextJson(renderContext, ignoreAttrs);

		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, renderContextJson);
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
	 * </pre>
	 * </code>
	 * <p>
	 * 注意：只会写入空JS对象。
	 * </p>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void writeNoAttributes(Writer out, RenderContext renderContext, String varName) throws IOException
	{
		NoAttributesRenderContextJson jsonRenderContext = new NoAttributesRenderContextJson(renderContext);

		out.write("var " + varName + "=");
		writeJsonObject(out, jsonRenderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 用于输出JSON的{@linkplain RenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class RenderContextJson extends DefaultRenderContext
	{
		private static final long serialVersionUID = 1L;

		public RenderContextJson(RenderContext renderContext)
		{
			this(renderContext, null);
		}

		public RenderContextJson(RenderContext renderContext, Collection<String> ignoreAttrs)
		{
			super();
			setAttributes(renderContext.getAttributes());

			if (ignoreAttrs != null)
			{
				for (String attr : ignoreAttrs)
					removeAttribute(attr);
			}
		}
	}

	/**
	 * 用于输出JSON且{@linkplain RenderContext#getAttributes()}为空的{@linkplain RenderContext}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class NoAttributesRenderContextJson extends DefaultRenderContext
	{
		private static final long serialVersionUID = 1L;

		public NoAttributesRenderContextJson(RenderContext renderContext)
		{
			super();
			super.setAttributes(Collections.emptyMap());
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
