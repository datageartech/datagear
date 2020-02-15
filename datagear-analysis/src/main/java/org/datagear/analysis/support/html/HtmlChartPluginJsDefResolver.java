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
		StringBuilder chartRendererBuilder = new StringBuilder();

		resolveJsDefContent(reader, jsonBuilder, chartRendererBuilder);

		return new JsDefContent(jsonBuilder.toString(), chartRendererBuilder.toString());
	}

	protected void resolveJsDefContent(Reader in, StringBuilder jsonBuilder, StringBuilder chartRendererBuilder)
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
					readChartRendererObjectContent(in, chartRendererBuilder);
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
	 * 从<code>{</code>之前的位置开始读取{@linkplain JsChartRendererer}内容。
	 * <p>
	 * 读取停止位置为：<code>}</code>
	 * </p>
	 * 
	 * @param in
	 * @param chartRendererBuilder
	 * @throws IOException
	 */
	protected void readChartRendererObjectContent(Reader in, StringBuilder chartRendererBuilder) throws IOException
	{
		int qcount = 0;

		int c = in.read();

		while (c > -1)
		{
			appendChar(chartRendererBuilder, c);

			if (c == '{')
			{
				qcount++;
				c = in.read();
			}
			else if (c == '}')
			{
				qcount--;

				if (qcount == 0)
					break;
				else
					c = in.read();
			}
			// 字符串
			else if (c == '\'' || c == '"')
			{
				c = readQuoted(in, chartRendererBuilder, c);
			}
			// 注释
			else if (c == '/')
			{
				c = in.read();

				// 行注释
				if (c == '/')
				{
					appendChar(chartRendererBuilder, c);
					c = skipLineComment(in, chartRendererBuilder, false);
				}
				else if (c == '*')
				{
					appendChar(chartRendererBuilder, c);
					c = skipBlockComment(in, chartRendererBuilder, false);
				}
			}
			else
				c = in.read();
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
