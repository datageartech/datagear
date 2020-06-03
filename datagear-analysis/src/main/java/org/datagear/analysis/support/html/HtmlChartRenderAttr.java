/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Serializable;
import java.io.Writer;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;

/**
 * {@linkplain HtmlChart}渲染上下文属性定义类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartRenderAttr extends HtmlRenderAttr
{
	private static final long serialVersionUID = 1L;

	/**
	 * {@linkplain HtmlChartRenderAttr}的渲染上下文属性名。
	 */
	public static final String ATTR_NAME = HtmlChartRenderAttr.class.getName();

	/** 属性名：渲染选项 */
	private String renderOptionName = "renderOption";

	public HtmlChartRenderAttr()
	{
		super();
	}

	public String getRenderOptionName()
	{
		return renderOptionName;
	}

	public void setRenderOptionName(String renderOptionName)
	{
		this.renderOptionName = renderOptionName;
	}

	/**
	 * 获取{@linkplain HtmlChartRenderOption}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public HtmlChartRenderOption getRenderOptionNonNull(RenderContext renderContext)
	{
		HtmlChartRenderOption renderOption = renderContext.getAttribute(this.renderOptionName);

		if (renderOption == null)
			throw new RenderException("The [" + this.renderOptionName + "] attribute must be set");

		return renderOption;
	}

	/**
	 * 获取{@linkplain HtmlChartRenderOption}，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 * @return
	 */
	public HtmlChartRenderOption getRenderOption(RenderContext renderContext)
	{
		return renderContext.getAttribute(this.renderOptionName);
	}

	/**
	 * 设置{@linkplain HtmlChartRenderOption}。
	 * 
	 * @param renderContext
	 * @param renderOption
	 */
	public void setRenderOption(RenderContext renderContext, HtmlChartRenderOption renderOption)
	{
		renderContext.setAttribute(this.renderOptionName, renderOption);
	}

	/**
	 * 移除{@linkplain HtmlChartRenderOption}。
	 * 
	 * @param renderContext
	 * @return 移除对象
	 */
	public HtmlChartRenderOption removeRenderOption(RenderContext renderContext)
	{
		return renderContext.removeAttribute(this.renderOptionName);
	}

	/**
	 * 设置{@linkplain HtmlChartPlugin#renderChart(RenderContext, ChartDefinition)}必须的上下文属性值。
	 * 
	 * @param renderContext
	 * @param htmlWriter
	 * @param renderOption
	 */
	public void inflate(RenderContext renderContext, Writer htmlWriter, HtmlChartRenderOption renderOption)
	{
		HtmlChartRenderAttr.set(renderContext, this);
		setHtmlWriter(renderContext, htmlWriter);
		setRenderOption(renderContext, renderOption);
	}

	/**
	 * 获取{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartRenderAttr getNonNull(RenderContext renderContext)
	{
		HtmlChartRenderAttr renderAttr = get(renderContext);

		if (renderAttr == null)
			throw new RenderException("The [" + ATTR_NAME + "] attribute must be set");

		return renderAttr;
	}

	/**
	 * 获取{@linkplain HtmlChartRenderAttr}对象，没有则返回{@code null}。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartRenderAttr get(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_NAME);
	}

	/**
	 * 设置{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 * @param renderAttr
	 */
	public static void set(RenderContext renderContext, HtmlChartRenderAttr renderAttr)
	{
		renderContext.setAttribute(ATTR_NAME, renderAttr);
	}

	/**
	 * 移除{@linkplain HtmlChartRenderAttr}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartRenderAttr remove(RenderContext renderContext)
	{
		return renderContext.removeAttribute(ATTR_NAME);
	}

	/**
	 * {@linkplain HtmlChart}渲染选项。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class HtmlChartRenderOption implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** 图表的HTML元素ID */
		private String chartElementId;

		/** 是否不输出图表HTML元素 */
		private boolean notWriteChartElement = false;

		/** 插件变量名 */
		private String pluginVarName;

		/** 是否不输出插件JS对象 */
		private boolean notWritePluginObject = false;

		/** 图表变量名 */
		private String chartVarName;

		/** 渲染上下文变量名 */
		private String renderContextVarName;

		/** 是否不输出渲染上下文JS对象 */
		private boolean notWriteRenderContextObject = false;

		/** 是否不输出{@code <script>}标签 */
		private boolean notWriteScriptTag = false;

		/** 是否不输出调用渲染函数 */
		private boolean notWriteInvoke = false;

		/** 写入图表JSON对象而非JS对象 */
		private boolean writeChartJson = false;

		public HtmlChartRenderOption()
		{
			super();
		}

		public HtmlChartRenderOption(HtmlRenderAttr renderAttr)
		{
			this(renderAttr.genChartElementId(), renderAttr.genChartPluginVarName(), renderAttr.genChartVarName(),
					renderAttr.genRenderContextVarName());
		}

		public HtmlChartRenderOption(String chartElementId, String pluginVarName, String chartVarName,
				String renderContextVarName)
		{
			super();
			this.chartElementId = chartElementId;
			this.pluginVarName = pluginVarName;
			this.chartVarName = chartVarName;
			this.renderContextVarName = renderContextVarName;
		}

		public String getChartElementId()
		{
			return chartElementId;
		}

		/**
		 * 自定义图表HTML元素的ID属性值。
		 * 
		 * @param chartElementId
		 */
		public void setChartElementId(String chartElementId)
		{
			this.chartElementId = chartElementId;
		}

		public boolean isNotWriteChartElement()
		{
			return notWriteChartElement;
		}

		/**
		 * 自定义是否不输出图表HTML元素。
		 * 
		 * @param notWriteChartElement
		 */
		public void setNotWriteChartElement(boolean notWriteChartElement)
		{
			this.notWriteChartElement = notWriteChartElement;
		}

		public String getPluginVarName()
		{
			return pluginVarName;
		}

		/**
		 * 自定义{@linkplain HtmlChartPlugin} JS变量名。
		 * 
		 * @param pluginVarName
		 */
		public void setPluginVarName(String pluginVarName)
		{
			this.pluginVarName = pluginVarName;
		}

		public boolean isNotWritePluginObject()
		{
			return notWritePluginObject;
		}

		/**
		 * 设置是否不输出{@linkplain HtmlChartPlugin} JS对象。
		 * 
		 * @param notWritePluginObject
		 */
		public void setNotWritePluginObject(boolean notWritePluginObject)
		{
			this.notWritePluginObject = notWritePluginObject;
		}

		public String getChartVarName()
		{
			return chartVarName;
		}

		/**
		 * 自定义{@linkplain HtmlChart} JS变量名。
		 * 
		 * @param pluginVarName
		 */
		public void setChartVarName(String chartVarName)
		{
			this.chartVarName = chartVarName;
		}

		public String getRenderContextVarName()
		{
			return renderContextVarName;
		}

		/**
		 * 自定义{@linkplain RenderContext} JS变量名。
		 * 
		 * @param renderContextVarName
		 */
		public void setRenderContextVarName(String renderContextVarName)
		{
			this.renderContextVarName = renderContextVarName;
		}

		public boolean isNotWriteRenderContextObject()
		{
			return notWriteRenderContextObject;
		}

		/**
		 * 设置是否不输出{@linkplain RenderContext} JS对象。
		 * 
		 * @param notWriteRenderContextObject
		 */
		public void setNotWriteRenderContextObject(boolean notWriteRenderContextObject)
		{
			this.notWriteRenderContextObject = notWriteRenderContextObject;
		}

		public boolean isNotWriteScriptTag()
		{
			return notWriteScriptTag;
		}

		/**
		 * 自定义是否不输出<code>&lt;script&gt;</code>标签。
		 * 
		 * @param notWriteScriptTag
		 */
		public void setNotWriteScriptTag(boolean notWriteScriptTag)
		{
			this.notWriteScriptTag = notWriteScriptTag;
		}

		public boolean isNotWriteInvoke()
		{
			return notWriteInvoke;
		}

		/**
		 * 自定义是否不输出调用渲染图表的函数。
		 * 
		 * @param notWriteInvoke
		 */
		public void setNotWriteInvoke(boolean notWriteInvoke)
		{
			this.notWriteInvoke = notWriteInvoke;
		}

		public boolean isWriteChartJson()
		{
			return writeChartJson;
		}

		/**
		 * 设置写入图表JSON对象而非JS对象。
		 * 
		 * @param writeChartJson
		 */
		public void setWriteChartJson(boolean writeChartJson)
		{
			this.writeChartJson = writeChartJson;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [chartElementId=" + chartElementId + ", notWriteChartElement="
					+ notWriteChartElement + ", pluginVarName=" + pluginVarName + ", notWritePluginObject="
					+ notWritePluginObject + ", chartVarName=" + chartVarName + ", renderContextVarName="
					+ renderContextVarName + ", notWriteRenderContextObject=" + notWriteRenderContextObject
					+ ", notWriteScriptTag=" + notWriteScriptTag + ", notWriteInvoke=" + notWriteInvoke
					+ ", writeChartJson=" + writeChartJson + "]";
		}
	}
}
