/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import org.datagear.util.IOUtil;

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
 * 	chartRender: {...},
 * 	...
 * }
 * </pre>
 * </code>
 * <p>
 * 此类从上述格式的输入流解析{@linkplain JsDefContent}对象，其中：
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginJson()}为上述格式中将<code>chartRender: {...}</code>替换为<code>chartRender: {}</code>的内容。
 * </p>
 * <p>
 * {@linkplain JsDefContent#getPluginChartRender()}为上述格式中<code>chartRender</code>属性值部分的内容：<code>{...}</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginJsDefResolver extends TextParserSupport
{
	protected static final char PROPERTY_CHART_RENDER_FIRST = HtmlChartPlugin.PROPERTY_CHART_RENDER.charAt(0);
	protected static final String PROPERTY_CHART_RENDER_DQ = "\"" + HtmlChartPlugin.PROPERTY_CHART_RENDER + "\"";
	protected static final String PROPERTY_CHART_RENDER_SQ = "'" + HtmlChartPlugin.PROPERTY_CHART_RENDER + "'";

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
		StringBuilder jsonBuilder = new StringBuilder();
		StringBuilder chartRenderBuilder = new StringBuilder();

		resolveJsDefContent(reader, jsonBuilder, chartRenderBuilder);

		return new JsDefContent(jsonBuilder.toString(), chartRenderBuilder.toString());
	}

	protected void resolveJsDefContent(Reader in, StringBuilder jsonBuilder, StringBuilder chartRenderBuilder)
			throws IOException
	{
		StringBuilder token = createStringBuilder();

		int c = in.read();
		while (c > -1)
		{
			appendChar(jsonBuilder, c);

			if (isWhitespace(c))
			{
				c = in.read();
			}
			else if (c == ':')
			{
				String tokenStr = token.toString();
				clear(token);

				if (tokenStr.equals(HtmlChartPlugin.PROPERTY_CHART_RENDER) || tokenStr.equals(PROPERTY_CHART_RENDER_DQ)
						|| tokenStr.equals(PROPERTY_CHART_RENDER_SQ))
				{
					readChartRenderObjectContent(in, chartRenderBuilder);
					jsonBuilder.append("{}");
				}

				c = in.read();
			}
			else if (c == '{' || c == ',')
			{
				clear(token);
				c = in.read();
			}
			// 注释
			else if (c == '/')
			{
				c = in.read();

				if (c == '/')
				{
					appendChar(jsonBuilder, c);
					c = skipLineComment(in, jsonBuilder, false);
				}
				else if (c == '*')
				{
					appendChar(jsonBuilder, c);
					c = skipBlockComment(in, jsonBuilder, false);
				}
				else
				{
					appendChar(token, '/');
					appendCharIfValid(token, c);
				}
			}
			// 字符串
			else if (c == '\'' || c == '"')
			{
				appendChar(token, c);
				c = readQuoted(in, token, c);

				jsonBuilder.append(token.substring(1));
			}
			else
			{
				appendChar(token, c);
				c = in.read();
			}
		}
	}

	/**
	 * 从<code>{</code>之前的位置开始读取{@linkplain JsChartRenderer}内容。
	 * <p>
	 * 读取停止位置为：<code>}</code>
	 * </p>
	 * 
	 * @param in
	 * @param chartRenderBuilder
	 * @throws IOException
	 */
	protected void readChartRenderObjectContent(Reader in, StringBuilder chartRenderBuilder) throws IOException
	{
		int qcount = 0;

		int c = -1;
		while ((c = in.read()) > -1)
		{
			appendChar(chartRenderBuilder, c);

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
			else if (c == '\'' || c == '"')
			{
				c = readQuoted(in, chartRenderBuilder, c);
				appendCharIfValid(chartRenderBuilder, c);
			}
			// 注释
			else if (c == '/')
			{
				int next = in.read();
				appendCharIfValid(chartRenderBuilder, next);

				// 行注释
				if (next == '/')
					skipLineComment(in, chartRenderBuilder, true);
				else if (next == '*')
					skipBlockComment(in, chartRenderBuilder, true);
			}
		}
	}

	public static class JsDefContent implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 插件定义JSON */
		private String pluginJson;

		/** 插件JS渲染器对象内容 */
		private String pluginChartRender;

		public JsDefContent()
		{
			super();
		}

		public JsDefContent(String pluginJson, String pluginChartRender)
		{
			super();
			this.pluginJson = pluginJson;
			this.pluginChartRender = pluginChartRender;
		}

		public String getPluginJson()
		{
			return pluginJson;
		}

		public void setPluginJson(String pluginJson)
		{
			this.pluginJson = pluginJson;
		}

		public String getPluginChartRender()
		{
			return pluginChartRender;
		}

		public void setPluginChartRender(String pluginChartRender)
		{
			this.pluginChartRender = pluginChartRender;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [pluginJson=" + pluginJson + ", pluginChartRender="
					+ pluginChartRender + "]";
		}
	}
}
