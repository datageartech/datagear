/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperty;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * {@linkplain HtmlChart}脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public HtmlChartScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlChart}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param chart
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChart chart) throws IOException
	{
		write(out, chart, null);
	}

	/**
	 * 将{@linkplain HtmlChart}以脚本对象格式（“<code>{...}</code>”）写入输出流。
	 * 
	 * @param out
	 * @param chart
	 * @param chartRenderContextVarName
	 *            不输出{@linkplain HtmlChart#getRenderContext()}实际对象，而输出已存在的{@linkplain RenderContext}变量名，为{@code null}则输出实际对象。
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChart chart, String chartRenderContextVarName) throws IOException
	{
		chart = new JsonHtmlChart(chart, chartRenderContextVarName);

		writeScriptObject(out, chart);
	}

	/**
	 * 仅用于JSON输出的{@linkplain HtmlChart}。
	 * <p>
	 * 为了支持{@linkplain HtmlRenderAttributes#setChartRenderContextVarName(RenderContext, String)}特性，
	 * 它会使用{@linkplain RefHtmlRenderContext}代替真正的{@linkplain HtmlChart#getRenderContext()}，
	 * 然后在输出时特殊处理。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonHtmlChart extends HtmlChart
	{
		public JsonHtmlChart(HtmlChart htmlChart)
		{
			this(htmlChart, null);
		}

		public JsonHtmlChart(HtmlChart htmlChart, String chartRenderContextVarName)
		{
			super(htmlChart.getId(),
					(StringUtil.isEmpty(chartRenderContextVarName)
							? new AttributesHtmlRenderContext(htmlChart.getRenderContext())
							: new RefHtmlRenderContext(chartRenderContextVarName)),
					new IdChartPlugin(htmlChart.getPlugin()), htmlChart.getPropertyValues(),
					JsonChartDataSet.valuesOf(htmlChart.getChartDataSets()), htmlChart.getElementId(),
					htmlChart.getVarName());
		}
	}

	protected static class IdChartPlugin extends AbstractIdentifiable implements ChartPlugin<RenderContext>
	{
		public IdChartPlugin(ChartPlugin<?> chartPlugin)
		{
			super(chartPlugin.getId());
		}

		@Override
		public Label getNameLabel()
		{
			return null;
		}

		@Override
		public Label getDescLabel()
		{
			return null;
		}

		@Override
		public Label getManualLabel()
		{
			return null;
		}

		@Override
		public Icon getIcon(RenderStyle renderStyle)
		{
			return null;
		}

		@Override
		public List<ChartProperty> getChartProperties()
		{
			return null;
		}

		@Override
		public ChartProperty getChartProperty(String name)
		{
			return null;
		}

		@Override
		public List<DataSign> getDataSigns()
		{
			return null;
		}

		@Override
		public DataSign getDataSign(String name)
		{
			return null;
		}

		@Override
		public String getVersion()
		{
			return null;
		}

		@Override
		public int getOrder()
		{
			return 0;
		}

		@Override
		public Chart renderChart(RenderContext renderContext, Map<String, ?> chartPropertyValues,
				ChartDataSet... chartDataSets) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class JsonChartDataSet extends ChartDataSet
	{
		public JsonChartDataSet(ChartDataSet chartDataSet)
		{
			super(new JsonDataSet(chartDataSet.getDataSet()));
			setPropertySigns(chartDataSet.getPropertySigns());
		}

		public static JsonChartDataSet[] valuesOf(ChartDataSet[] chartDataSets)
		{
			if (chartDataSets == null)
				return null;

			JsonChartDataSet[] jsonDataSets = new JsonChartDataSet[chartDataSets.length];

			for (int i = 0; i < chartDataSets.length; i++)
				jsonDataSets[i] = new JsonChartDataSet(chartDataSets[i]);

			return jsonDataSets;
		}
	}

	protected static class JsonDataSet extends AbstractDataSet
	{
		public JsonDataSet(DataSet dataSet)
		{
			super(dataSet.getId(), dataSet.getProperties());
			setParams(dataSet.getParams());
			setExports(dataSet.getExports());
		}

		@Override
		public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
		{
			return null;
		}
	}
}
