/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperties;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.DataSetParamValues;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractDataSetFactory;
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
			super(htmlChart.getId(), new IdChartPlugin(htmlChart.getPlugin()),
					(StringUtil.isEmpty(chartRenderContextVarName)
							? new AttributesHtmlRenderContext(htmlChart.getRenderContext())
							: new RefHtmlRenderContext(chartRenderContextVarName)),
					htmlChart.getPropertyValues(), JsonDataSetFactory.valuesOf(htmlChart.getDataSetFactories()),
					htmlChart.getElementId(), htmlChart.getVarName());
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
		public ChartProperties getChartProperties()
		{
			return null;
		}

		@Override
		public int getOrder()
		{
			return 0;
		}

		@Override
		public Chart renderChart(RenderContext renderContext, ChartPropertyValues chartPropertyValues,
				DataSetFactory... dataSetFactories) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	protected static class JsonDataSetFactory extends AbstractDataSetFactory
	{
		public JsonDataSetFactory(DataSetFactory dataSetFactory)
		{
			super(dataSetFactory.getId());
			setParams(dataSetFactory.getParams());
			setExports(dataSetFactory.getExports());
		}

		@Override
		public DataSet getDataSet(DataSetParamValues dataSetParamValues) throws DataSetException
		{
			return null;
		}

		public static JsonDataSetFactory[] valuesOf(DataSetFactory[] dataSetFactories)
		{
			if (dataSetFactories == null)
				return null;

			JsonDataSetFactory[] jsonDataSetFactories = new JsonDataSetFactory[dataSetFactories.length];

			for (int i = 0; i < dataSetFactories.length; i++)
				jsonDataSetFactories[i] = new JsonDataSetFactory(dataSetFactories[i]);

			return jsonDataSetFactories;
		}
	}
}
