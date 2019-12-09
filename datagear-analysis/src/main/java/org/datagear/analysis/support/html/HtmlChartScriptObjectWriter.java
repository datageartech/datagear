/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Chart;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperties;
import org.datagear.analysis.ChartPropertyValues;
import org.datagear.analysis.DataSetFactory;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.AbstractRenderContext;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * {@linkplain HtmlChart}脚本对象输出流。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartScriptObjectWriter
{
	private static final SerializerFeature[] DEFAULT_SERIALIZER_FEATURES = new SerializerFeature[] {
			SerializerFeature.QuoteFieldNames, SerializerFeature.WriteEnumUsingName };

	private SerializerFeature[] serializerFeatures = DEFAULT_SERIALIZER_FEATURES;

	private SerializeConfig serializeConfig = new SerializeConfig();

	public HtmlChartScriptObjectWriter()
	{
		super();
		initSerializeConfig(this.serializeConfig);
	}

	public SerializerFeature[] getSerializerFeatures()
	{
		return serializerFeatures;
	}

	public void setSerializerFeatures(SerializerFeature[] serializerFeatures)
	{
		this.serializerFeatures = serializerFeatures;
	}

	protected SerializeConfig getSerializeConfig()
	{
		return serializeConfig;
	}

	protected void initSerializeConfig(SerializeConfig serializeConfig)
	{
		RefRenderContextSerializer refRenderContextSerializer = new RefRenderContextSerializer();

		serializeConfig.put(RefRenderContext.class, refRenderContextSerializer);
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
	 * 写{@linkplain HtmlChart}脚本对象。
	 * 
	 * @param out
	 * @param chart
	 * @throws IOException
	 */
	protected void writeScriptObject(Writer out, HtmlChart chart) throws IOException
	{
		SerializeWriter serializeWriter = new SerializeWriter(out, this.serializerFeatures);
		JSONSerializer serializer = new JSONSerializer(serializeWriter, this.serializeConfig);

		try
		{
			serializer.write(chart);
		}
		finally
		{
			serializeWriter.flush();
		}
	}

	/**
	 * 仅用于JSON输出的{@linkplain HtmlChart}。
	 * <p>
	 * 为了支持{@linkplain HtmlRenderAttributes#setChartRenderContextVarName(RenderContext, String)}特性，
	 * 它会使用{@linkplain RefRenderContext}代替真正的{@linkplain HtmlChart#getRenderContext()}，
	 * 然后在输出时特殊处理。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class JsonHtmlChart extends HtmlChart
	{
		public JsonHtmlChart()
		{
			super();
		}

		public JsonHtmlChart(HtmlChart htmlChart)
		{
			this(htmlChart, null);
		}

		public JsonHtmlChart(HtmlChart htmlChart, String chartRenderContextVarName)
		{
			super(htmlChart.getId(), new IdChartPlugin(htmlChart.getChartPlugin()),
					(StringUtil.isEmpty(chartRenderContextVarName)
							? new AttributesRenderContext(htmlChart.getRenderContext())
							: new RefRenderContext(chartRenderContextVarName)),
					htmlChart.getChartPropertyValues(), htmlChart.getDataSetFactories(), htmlChart.getChartElementId(),
					htmlChart.getChartVarName());
		}
	}

	protected static class IdChartPlugin extends AbstractIdentifiable implements ChartPlugin<RenderContext>
	{
		public IdChartPlugin()
		{
			super();
		}

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
		public Chart renderChart(RenderContext renderContext, ChartPropertyValues chartPropertyValues,
				DataSetFactory... dataSetFactories) throws RenderException
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 仅带有{@linkplain RenderContext#getAttributes()}的{@linkplain RenderContext}。
	 * <p>
	 * 此类仅用于脚本输出。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class AttributesRenderContext extends AbstractRenderContext
	{
		public AttributesRenderContext()
		{
			super();
		}

		public AttributesRenderContext(RenderContext renderContext)
		{
			super(renderContext.getAttributes());
		}

		@Override
		public <T> T getAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAttribute(String name, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T removeAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 引用名{@linkplain RenderContext}。
	 * <p>
	 * 此类仅用于脚本输出。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class RefRenderContext implements RenderContext
	{
		private String refName;

		public RefRenderContext()
		{
			super();
		}

		public RefRenderContext(String refName)
		{
			super();
			this.refName = refName;
		}

		public String getRefName()
		{
			return refName;
		}

		public void setRefName(String refName)
		{
			this.refName = refName;
		}

		@Override
		public <T> T getAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void setAttribute(String name, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T removeAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasAttribute(String name)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, ?> getAttributes()
		{
			return null;
		}
	}

	protected static class RefRenderContextSerializer implements ObjectSerializer
	{
		@Override
		public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
				throws IOException
		{
			String refName = null;

			if (object != null)
			{
				RefRenderContext refRenderContext = (RefRenderContext) object;
				refName = refRenderContext.getRefName();
			}

			serializer.getWriter().append(refName);
		}
	}
}
