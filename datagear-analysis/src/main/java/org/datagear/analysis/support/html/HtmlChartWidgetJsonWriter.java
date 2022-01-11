/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.DefaultRenderContext;
import org.datagear.analysis.support.html.HtmlChartRenderAttr.HtmlChartRenderOption;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlChartWidget} JSON输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetJsonWriter
{
	public HtmlChartWidgetJsonWriter()
	{
		super();
	}

	/**
	 * 输出JSON。
	 * 
	 * @param out
	 * @param chartWidget
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart write(Writer out, HtmlChartWidget chartWidget) throws RenderException, IOException
	{
		return writeJsonSetting(out, new HtmlChartWidgetJsonSetting(chartWidget));
	}

	/**
	 * 输出JSON。
	 * 
	 * @param out
	 * @param jsonSetting
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart writeJsonSetting(Writer out, HtmlChartWidgetJsonSetting jsonSetting)
			throws RenderException, IOException
	{
		HtmlChartWidget chartWidget = jsonSetting.getChartWidget();

		RenderContext renderContext = new DefaultRenderContext();
		HtmlChartRenderAttr renderAttr = new HtmlChartRenderAttr();
		HtmlChartRenderOption renderOption = new HtmlChartRenderOption(jsonSetting.getChartElementId(),
				genPluginVarName(chartWidget), jsonSetting.getChartVarName(), jsonSetting.getRenderContextVarName());
		renderOption.setNotWriteChartElement(true);
		renderOption.setNotWritePluginObject(true);
		renderOption.setNotWriteRenderContextObject(true);
		renderOption.setNotWriteScriptTag(true);
		renderOption.setNotWriteInvoke(true);
		renderOption.setWriteChartJson(true);
		renderAttr.inflate(renderContext, out, renderOption);

		HtmlChart chart = chartWidget.render(renderContext);

		return chart;
	}

	/**
	 * 输出JSON。
	 * 
	 * @param out
	 * @param chartWidgets
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart[] write(Writer out, HtmlChartWidget... chartWidgets) throws RenderException, IOException
	{
		List<HtmlChartWidget> list = Arrays.asList(chartWidgets);
		List<HtmlChart> charts = write(out, list);

		return charts.toArray(new HtmlChart[charts.size()]);
	}

	/**
	 * 输出JSON。
	 * 
	 * @param out
	 * @param chartWidgets
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public List<HtmlChart> write(Writer out, List<? extends HtmlChartWidget> chartWidgets)
			throws RenderException, IOException
	{
		List<HtmlChartWidgetJsonSetting> jsonSettings = new ArrayList<>(chartWidgets.size());
		for (HtmlChartWidget chartWidget : chartWidgets)
			jsonSettings.add(new HtmlChartWidgetJsonSetting(chartWidget));

		return writeJsonSetting(out, jsonSettings);
	}

	/**
	 * 输出JSON。
	 * 
	 * @param out
	 * @param jsonSettings
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public List<HtmlChart> writeJsonSetting(Writer out, List<? extends HtmlChartWidgetJsonSetting> jsonSettings)
			throws RenderException, IOException
	{
		List<HtmlChart> htmlCharts = new ArrayList<>(jsonSettings.size());

		RenderContext renderContext = new DefaultRenderContext();

		HtmlChartRenderAttr renderAttr = new HtmlChartRenderAttr();
		HtmlChartRenderOption renderOption = new HtmlChartRenderOption("", "", "", "");
		renderOption.setNotWriteChartElement(true);
		renderOption.setNotWritePluginObject(true);
		renderOption.setNotWriteRenderContextObject(true);
		renderOption.setNotWriteScriptTag(true);
		renderOption.setNotWriteInvoke(true);
		renderOption.setWriteChartJson(true);
		renderAttr.inflate(renderContext, out, renderOption);

		out.write('[');

		for (int i = 0, len = jsonSettings.size(); i < len; i++)
		{
			HtmlChartWidgetJsonSetting jsonSetting = jsonSettings.get(i);
			HtmlChartWidget chartWidget = jsonSetting.getChartWidget();

			renderOption.setChartElementId(jsonSetting.getChartElementId());
			renderOption.setPluginVarName(genPluginVarName(chartWidget));
			renderOption.setChartVarName(jsonSetting.getChartVarName());
			renderOption.setRenderContextVarName(jsonSetting.getRenderContextVarName());

			HtmlChart chart = chartWidget.render(renderContext);

			if (i < len - 1)
				out.write(", ");

			htmlCharts.add(chart);
		}

		out.write(']');

		return htmlCharts;
	}

	protected String genPluginVarName(HtmlChartWidget chartWidget)
	{
		return "{\"id\": " + StringUtil.toJavaScriptString(chartWidget.getPlugin().getId()) + "}";
	}

	/**
	 * {@linkplain HtmlChartWidget} JSON输出设置。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartWidgetJsonSetting
	{
		private HtmlChartWidget chartWidget;

		private String chartElementId = "";

		private String chartVarName = "";

		private String renderContextVarName = "{}";

		public HtmlChartWidgetJsonSetting()
		{
			super();
		}

		public HtmlChartWidgetJsonSetting(HtmlChartWidget chartWidget)
		{
			super();
			this.chartWidget = chartWidget;
		}

		public HtmlChartWidget getChartWidget()
		{
			return chartWidget;
		}

		public void setChartWidget(HtmlChartWidget chartWidget)
		{
			this.chartWidget = chartWidget;
		}

		public String getChartElementId()
		{
			return chartElementId;
		}

		public void setChartElementId(String chartElementId)
		{
			this.chartElementId = chartElementId;
		}

		public String getChartVarName()
		{
			return chartVarName;
		}

		public void setChartVarName(String chartVarName)
		{
			this.chartVarName = chartVarName;
		}

		public String getRenderContextVarName()
		{
			return renderContextVarName;
		}

		public void setRenderContextVarName(String renderContextVarName)
		{
			this.renderContextVarName = renderContextVarName;
		}
	}
}
