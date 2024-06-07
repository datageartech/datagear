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

import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.AbstractDataSet;
import org.datagear.analysis.support.DataFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain HtmlChart} JS脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartScriptObjectWriter extends AbstractHtmlScriptObjectWriter
{
	public static final HtmlChartScriptObjectWriter INSTANCE = new HtmlChartScriptObjectWriter();

	public HtmlChartScriptObjectWriter()
	{
		super();
	}

	/**
	 * 将{@linkplain HtmlChart}的JS脚本对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * var [chart.varName]=
	 * {
	 * 	...,
	 * 	renderContext : [renderContextVarName],
	 * 	plugin : [pluginVarName],
	 * 	...
	 * };
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param chart
	 * @param renderContextVarName
	 * @param pluginVarName
	 * @throws IOException
	 */
	public void write(Writer out, HtmlChart chart, String renderContextVarName, String pluginVarName) throws IOException
	{
		chart = toHtmlChartJson(chart, renderContextVarName, pluginVarName);

		out.write("var " + chart.getVarName() + "=");
		writeNewLine(out);
		writeJsonObject(out, chart);
		out.write(";");
		writeNewLine(out);
	}

	/**
	 * 将{@linkplain HtmlChart}的JSON对象写入输出流。
	 * <p>
	 * 格式为：
	 * </p>
	 * <code>
	 * <pre>
	 * {
	 * 	...,
	 * 	renderContext : [renderContextVarName],
	 * 	plugin : [pluginVarName],
	 * 	...
	 * }
	 * <pre>
	 * </code>
	 * 
	 * @param out
	 * @param chart
	 * @param renderContextVarName
	 * @param pluginVarName
	 * @throws IOException
	 */
	public void writeJson(Writer out, HtmlChart chart, String renderContextVarName, String pluginVarName)
			throws IOException
	{
		chart = toHtmlChartJson(chart, renderContextVarName, pluginVarName);

		writeJsonObject(out, chart);
	}

	/**
	 * 转换为{@linkplain HtmlChartJson}。
	 * 
	 * @param chart
	 * @param renderContextVarName
	 * @param pluginVarName
	 * @return
	 */
	protected HtmlChartJson toHtmlChartJson(HtmlChart chart, String renderContextVarName, String pluginVarName)
	{
		return new HtmlChartJson(chart, renderContextVarName, pluginVarName);
	}

	/**
	 * 用于输出JSON的{@linkplain HtmlChart}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class HtmlChartJson extends HtmlChart
	{
		private static final long serialVersionUID = 1L;

		public HtmlChartJson(HtmlChart htmlChart, String renderContextVarName, String pluginVarName)
		{
			super(htmlChart);
			setDataSetBinds(toDataSetBindJsons(htmlChart.getDataSetBinds()));
			setPlugin(toRefHtmlChartPlugin(pluginVarName));
			setRenderContext(toRefRenderContext(renderContextVarName));
		}

		protected DataSetBindJson[] toDataSetBindJsons(DataSetBind[] dataSetBinds)
		{
			if (dataSetBinds == null)
				return null;

			DataSetBindJson[] re = new DataSetBindJson[dataSetBinds.length];

			for (int i = 0; i < dataSetBinds.length; i++)
				re[i] = toDataSetBindJson(dataSetBinds[i]);

			return re;
		}

		protected DataSetBindJson toDataSetBindJson(DataSetBind dataSetBind)
		{
			return new DataSetBindJson(dataSetBind);
		}

		protected RefHtmlChartPlugin toRefHtmlChartPlugin(String pluginVarName)
		{
			return new RefHtmlChartPlugin(pluginVarName);
		}

		protected RefRenderContext toRefRenderContext(String renderContextVarName)
		{
			return new RefRenderContext(renderContextVarName);
		}
	}

	/**
	 * 用于输出JSON的{@linkplain DataSetBind}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class DataSetBindJson extends DataSetBind
	{
		private static final long serialVersionUID = 1L;

		public DataSetBindJson(DataSetBind dataSetBind)
		{
			super();
			setDataSet(toDataSetJson(dataSetBind.getDataSet()));
			setFieldSigns(dataSetBind.getFieldSigns());
			setAlias(dataSetBind.getAlias());
			setAttachment(dataSetBind.isAttachment());
			setQuery(dataSetBind.getQuery());
			setFieldAliases(dataSetBind.getFieldAliases());
			setFieldOrders(dataSetBind.getFieldOrders());
		}

		protected DataSetJson toDataSetJson(DataSet dataSet)
		{
			return new DataSetJson(dataSet);
		}

		@JsonIgnore
		@Override
		public DataSetResult getResult()
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 用于输出JSON的{@linkplain DataSet}。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class DataSetJson extends AbstractDataSet
	{
		private static final long serialVersionUID = 1L;

		public DataSetJson(DataSet dataSet)
		{
			super(dataSet.getId(), dataSet.getName(), dataSet.getFields());
			setMutableModel(dataSet.isMutableModel());
			setParams(dataSet.getParams());
		}

		/**
		 * JSON输出不需要底层数据转换格式信息
		 */
		@JsonIgnore
		@Override
		public DataFormat getDataFormat()
		{
			return super.getDataFormat();
		}

		@JsonIgnore
		@Override
		public DataSetResult getResult(DataSetQuery query) throws DataSetException
		{
			return null;
		}
	}
}
