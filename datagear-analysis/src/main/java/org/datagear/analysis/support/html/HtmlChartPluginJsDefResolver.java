/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import org.datagear.util.IOUtil;
import org.datagear.util.StringBuilderWriter;
import org.datagear.util.TextParserSupport;

/**
 * {@linkplain HtmlChartPlugin}的JS定义格式解析器。
 * <p>
 * 支持的JS定义格式：
 * </p>
 * <code>
 * <pre>
 * {
 * 	id: ...,
 * 	nameLabel: ...,
 * 	...,
 * 	chartRenderer: {...},
 * 	...
 * }
 * </pre>
 * </code>
 * <p>
 * 此类从上述格式的输入流解析{@linkplain JsDefContent}对象，其中：
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginJson()}为上述格式中将
 * </p>
 * <p>
 * <code>chartRenderer: {...}</code>
 * </p>
 * <p>
 * 替换为
 * </p>
 * <p>
 * <code>chartRenderer: {}</code>
 * </p>
 * <p>
 * 的内容。
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginChartRenderer()}为上述格式中<code>chartRenderer</code>属性值的内容：
 * </p>
 * <p>
 * <code>{...}</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginJsDefResolver extends TextParserSupport
{
	protected static final char PROPERTY_CHART_RENDERER_FIRST = HtmlChartPlugin.PROPERTY_CHART_RENDERER.charAt(0);
	protected static final String PROPERTY_CHART_RENDERER_DQ = "\"" + HtmlChartPlugin.PROPERTY_CHART_RENDERER + "\"";
	protected static final String PROPERTY_CHART_RENDERER_SQ = "'" + HtmlChartPlugin.PROPERTY_CHART_RENDERER + "'";

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
		StringBuilderWriter jsonOut = new StringBuilderWriter();
		StringBuilderWriter chartRendererOut = new StringBuilderWriter();

		resolveJsDefContent(reader, jsonOut, chartRendererOut);

		return new JsDefContent(jsonOut.getString(), chartRendererOut.getString());
	}

	protected void resolveJsDefContent(Reader in, Writer jsonOut, Writer chartRendererOut) throws IOException
	{
		StringBuilder prevToken = new StringBuilder();
		boolean chartRendererResolved = false;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			jsonOut.write(c);

			if (!chartRendererResolved)
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

					if (prevStr.equals(HtmlChartPlugin.PROPERTY_CHART_RENDERER)
							|| prevStr.equals(PROPERTY_CHART_RENDERER_DQ) || prevStr.equals(PROPERTY_CHART_RENDERER_SQ))
					{
						writeAfterJsObject(in, chartRendererOut, false);
						jsonOut.append("{}");
						chartRendererResolved = true;
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

	public static class JsDefContent implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 插件定义JSON */
		private String pluginJson;

		/** 插件JS渲染器对象内容 */
		private String pluginChartRenderer;

		public JsDefContent()
		{
			super();
		}

		public JsDefContent(String pluginJson, String pluginChartRenderer)
		{
			super();
			this.pluginJson = pluginJson;
			this.pluginChartRenderer = pluginChartRenderer;
		}

		public String getPluginJson()
		{
			return pluginJson;
		}

		public void setPluginJson(String pluginJson)
		{
			this.pluginJson = pluginJson;
		}

		public String getPluginChartRenderer()
		{
			return pluginChartRenderer;
		}

		public void setPluginChartRenderer(String pluginChartRenderer)
		{
			this.pluginChartRenderer = pluginChartRenderer;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [pluginJson=" + pluginJson + ", pluginChartRenderer="
					+ pluginChartRenderer + "]";
		}
	}
}
