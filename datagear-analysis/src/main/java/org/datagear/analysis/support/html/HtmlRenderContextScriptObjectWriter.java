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

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;

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
	 *   "..." : ...
	 * };
	 * </pre>
	 * </code>
	 * 
	 * @param out
	 * @param renderContext
	 * @param varName
	 * @throws IOException
	 */
	public void write(Writer out, RenderContext renderContext, String varName) throws IOException
	{
		out.write("var " + varName + "=");
		writeNewLine(out);
		writeJsonObject(out, renderContext);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 将一个空{@linkplain RenderContext}的JS脚本对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [varName]= {};
	 * </pre>
	 * </code>
	 * 
	 * @param out
	 * @param varName
	 * @throws IOException
	 */
	public void writeEmpty(Writer out, String varName) throws IOException
	{
		out.write("var " + varName + "= {};");
		writeNewLine(out);
	}
}
