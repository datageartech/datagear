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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.TextParserSupport;

/**
 * {@linkplain HtmlChartPlugin}的JS定义格式解析器。
 * <p>
 * 此类用于从{@linkplain HtmlChartPluginLoader}定义的<code>plugin.json</code>中解析{@linkplain JsDefContent}对象，其中：
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginJson()}为<code>plugin.json</code>中将
 * </p>
 * <p>
 * <code>renderer: {...}</code>
 * <br>或<br>
 * <code>chartRenderer: {...}</code>
 * </p>
 * <p>
 * 替换为
 * </p>
 * <p>
 * <code>renderer: {}</code>
 * </p>
 * <p>
 * 的内容；
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginRenderer()}为上述格式中<code>renderer</code>（或<code>chartRenderer</code>）属性值内容：
 * </p>
 * <p>
 * <code>{...}</code>
 * </p>
 * <p>
 * 注意：如果<code>plugin.json</code>没有定义<code>renderer</code>（或<code>chartRenderer</code>）属性，
 * {@linkplain JsDefContent#hasPluginRenderer()}将为{@code false}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginJsDefResolver extends TextParserSupport
{
	protected static final String PROPERTY_RENDERER_DQ = "\"" + HtmlChartPlugin.PROPERTY_RENDERER + "\"";
	protected static final String PROPERTY_RENDERER_SQ = "'" + HtmlChartPlugin.PROPERTY_RENDERER + "'";

	protected static final String PROPERTY_RENDERER_OLD_DQ = "\"" + HtmlChartPlugin.PROPERTY_RENDERER_OLD + "\"";
	protected static final String PROPERTY_RENDERER_OLD_SQ = "'" + HtmlChartPlugin.PROPERTY_RENDERER_OLD + "'";

	public HtmlChartPluginJsDefResolver()
	{
		super();
	}

	/**
	 * 解析。
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public JsDefContent resolve(String str) throws IOException
	{
		if (str == null)
			str = "";

		Reader reader = null;

		try
		{
			reader = IOUtil.getReader(str);
			return resolve(reader);
		}
		finally
		{
			IOUtil.close(reader);
		}
	}

	/**
	 * 解析。
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public JsDefContent resolve(Reader reader) throws IOException
	{
		StringWriter jsonOut = new StringWriter();
		StringWriter rendererOut = new StringWriter();

		resolveJsDefContent(reader, jsonOut, rendererOut);

		JsDefContent jsDefContent = new JsDefContent(jsonOut.toString());
		jsDefContent.setPluginRenderer(rendererOut.toString());
		
		return jsDefContent;
	}

	protected void resolveJsDefContent(Reader in, Writer jsonOut, Writer rendererOut) throws IOException
	{
		StringBuilder prevToken = new StringBuilder();
		boolean rendererResolved = false;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			jsonOut.write(c);

			if (!rendererResolved)
			{
				// 字符串
				if (isJsQuote(c))
				{
					prevToken = availableStringBuilder(prevToken);
					appendChar(prevToken, c);

					writeAfterQuote(in, jsonOut, c, '\\', prevToken);
				}
				else if (c == '{' || c == ',')
				{
					prevToken = availableStringBuilder(prevToken);
				}
				else if (c == '/')
				{
					int c0 = in.read();
					writeIfValid(jsonOut, c0);

					// 行注释
					if (c0 == '/')
					{
						writeAfterLineComment(in, jsonOut);
					}
					// 块注释
					else if (c0 == '*')
					{
						writeAfterBlockComment(in, jsonOut);
					}
					else
					{
						appendChar(prevToken, c);
						appendCharIfValid(prevToken, c0);
					}
				}
				else if (c == ':')
				{
					String prevStr = prevToken.toString();

					if (prevStr.equals(HtmlChartPlugin.PROPERTY_RENDERER)
							|| prevStr.equals(PROPERTY_RENDERER_DQ) || prevStr.equals(PROPERTY_RENDERER_SQ)
							//兼容旧版本的"chartRenderer"渲染器属性名
							|| prevStr.equals(HtmlChartPlugin.PROPERTY_RENDERER_OLD)
							|| prevStr.equals(PROPERTY_RENDERER_OLD_DQ) || prevStr.equals(PROPERTY_RENDERER_OLD_SQ))
					{
						writeAfterJsObject(in, rendererOut, false);
						jsonOut.append("{}");
						rendererResolved = true;
					}
				}
				else
				{
					if (!isWhitespace(c))
						appendChar(prevToken, c);
				}
			}
		}
	}

	/**
	 * 写完下一个JavaScript对象后停止。
	 * 
	 * @param in
	 * @param out
	 * @param leftBraceWrote
	 * @throws IOException
	 */
	protected void writeAfterJsObject(Reader in, Writer out, boolean leftBraceWrote) throws IOException
	{
		int qcount = (leftBraceWrote ? 1 : 0);
		int c = -1;

		while ((c = in.read()) > -1)
		{
			out.write(c);

			if (c == '{')
			{
				qcount++;
			}
			else if (c == '}')
			{
				qcount--;

				if (qcount == 0)
					break;
			}
			// 字符串
			else if (isJsQuote(c))
			{
				writeAfterQuote(in, out, c, '\\');
			}
			// 注释
			else if (c == '/')
			{
				int c0 = in.read();
				writeIfValid(out, c0);

				// 行注释
				if (c0 == '/')
				{
					writeAfterLineComment(in, out);
				}
				else if (c == '*')
				{
					writeAfterBlockComment(in, out);
				}
			}
		}
	}

	public static class JsDefContent
	{
		/** 插件定义JSON */
		private String pluginJson;

		/** 插件JS渲染器对象内容 */
		private String pluginRenderer = null;

		public JsDefContent()
		{
			super();
		}

		public JsDefContent(String pluginJson)
		{
			super();
			this.pluginJson = pluginJson;
		}

		public String getPluginJson()
		{
			return pluginJson;
		}

		public void setPluginJson(String pluginJson)
		{
			this.pluginJson = pluginJson;
		}
		
		public boolean hasPluginRenderer()
		{
			return !StringUtil.isEmpty(this.pluginRenderer);
		}

		public String getPluginRenderer()
		{
			return pluginRenderer;
		}

		public void setPluginRenderer(String pluginRenderer)
		{
			this.pluginRenderer = pluginRenderer;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [pluginJson=" + pluginJson + ", pluginRenderer="
					+ pluginRenderer + "]";
		}
	}
}
