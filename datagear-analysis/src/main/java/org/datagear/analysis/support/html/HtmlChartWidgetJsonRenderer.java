/*
 * Copyright 2018-2024 datagear.tech
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.datagear.analysis.RenderException;
import org.datagear.util.StringUtil;

/**
 * {@linkplain HtmlChartWidget} JSON渲染器。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetJsonRenderer
{
	public HtmlChartWidgetJsonRenderer()
	{
		super();
	}

	/**
	 * 渲染JSON。
	 * 
	 * @param out
	 * @param chartWidget
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart render(Writer out, HtmlChartWidget chartWidget) throws RenderException, IOException
	{
		return renderJsonSetting(out, new HtmlChartWidgetJsonSetting(chartWidget));
	}

	/**
	 * 渲染JSON。
	 * 
	 * @param out
	 * @param jsonSetting
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart renderJsonSetting(Writer out, HtmlChartWidgetJsonSetting jsonSetting)
			throws RenderException, IOException
	{
		HtmlChartWidget chartWidget = jsonSetting.getChartWidget();
		HtmlChartRenderContext renderContext = new HtmlChartRenderContext(out);
		
		renderContext.setChartElementId(jsonSetting.getChartElementId());
		renderContext.setPluginVarName(genPluginVarName(chartWidget));
		renderContext.setChartVarName(jsonSetting.getChartVarName());
		renderContext.setRenderContextVarName(jsonSetting.getRenderContextVarName());
		renderContext.setNotWriteChartElement(true);
		renderContext.setNotWritePluginObject(true);
		renderContext.setNotWriteRenderContextObject(true);
		renderContext.setNotWriteScriptTag(true);
		renderContext.setNotWriteInvoke(true);
		renderContext.setWriteChartJson(true);

		HtmlChart chart = chartWidget.render(renderContext);

		return chart;
	}

	/**
	 * 渲染JSON。
	 * 
	 * @param out
	 * @param chartWidgets
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public HtmlChart[] render(Writer out, HtmlChartWidget... chartWidgets) throws RenderException, IOException
	{
		List<HtmlChartWidget> list = Arrays.asList(chartWidgets);
		List<HtmlChart> charts = render(out, list);

		return charts.toArray(new HtmlChart[charts.size()]);
	}

	/**
	 * 渲染JSON。
	 * 
	 * @param out
	 * @param chartWidgets
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public List<HtmlChart> render(Writer out, List<? extends HtmlChartWidget> chartWidgets)
			throws RenderException, IOException
	{
		List<HtmlChartWidgetJsonSetting> jsonSettings = new ArrayList<>(chartWidgets.size());
		for (HtmlChartWidget chartWidget : chartWidgets)
			jsonSettings.add(new HtmlChartWidgetJsonSetting(chartWidget));

		return renderJsonSetting(out, jsonSettings);
	}

	/**
	 * 渲染JSON。
	 * 
	 * @param out
	 * @param jsonSettings
	 * @return
	 * @throws RenderException
	 * @throws IOException
	 */
	public List<HtmlChart> renderJsonSetting(Writer out, List<? extends HtmlChartWidgetJsonSetting> jsonSettings)
			throws RenderException, IOException
	{
		List<HtmlChart> htmlCharts = new ArrayList<>(jsonSettings.size());
		
		HtmlChartRenderContext renderContext = new HtmlChartRenderContext(out);
		renderContext.setNotWriteChartElement(true);
		renderContext.setNotWritePluginObject(true);
		renderContext.setNotWriteRenderContextObject(true);
		renderContext.setNotWriteScriptTag(true);
		renderContext.setNotWriteInvoke(true);
		renderContext.setWriteChartJson(true);

		out.write('[');

		for (int i = 0, len = jsonSettings.size(); i < len; i++)
		{
			HtmlChartWidgetJsonSetting jsonSetting = jsonSettings.get(i);
			HtmlChartWidget chartWidget = jsonSetting.getChartWidget();

			renderContext.setChartElementId(jsonSetting.getChartElementId());
			renderContext.setPluginVarName(genPluginVarName(chartWidget));
			renderContext.setChartVarName(jsonSetting.getChartVarName());
			renderContext.setRenderContextVarName(jsonSetting.getRenderContextVarName());

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
