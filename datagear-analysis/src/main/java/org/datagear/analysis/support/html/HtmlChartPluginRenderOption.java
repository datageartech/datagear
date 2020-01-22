/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.Serializable;

import org.datagear.analysis.RenderContext;

/**
 * {@linkplain HtmlChartPlugin}渲染设置项。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginRenderOption implements Serializable
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

	public HtmlChartPluginRenderOption()
	{
		super();
	}

	public boolean hasChartElementId()
	{
		return (this.chartElementId != null && !this.chartElementId.isEmpty());
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
	 * <p>
	 * 如果设置为{@code true}，那么必须设置{@linkplain #setChartElementId(String)}。
	 * </p>
	 * 
	 * @param notWriteChartElement
	 */
	public void setNotWriteChartElement(boolean notWriteChartElement)
	{
		this.notWriteChartElement = notWriteChartElement;
	}

	public boolean hasPluginVarName()
	{
		return (this.pluginVarName != null && !this.pluginVarName.isEmpty());
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
	 * <p>
	 * 如果设置为{@code true}，那么必须设置{@linkplain #setPluginVarName(String)}。
	 * </p>
	 * 
	 * @param notWritePluginObject
	 */
	public void setNotWritePluginObject(boolean notWritePluginObject)
	{
		this.notWritePluginObject = notWritePluginObject;
	}

	public boolean hasChartVarName()
	{
		return (this.chartVarName != null && !this.chartVarName.isEmpty());
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

	public boolean hasRenderContextVarName()
	{
		return (this.renderContextVarName != null && !this.renderContextVarName.isEmpty());
	}

	public String getRenderContextVarName()
	{
		return renderContextVarName;
	}

	/**
	 * 自定义{@linkplain HtmlRenderContext} JS变量名。
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
	 * 设置是否不输出{@linkplain HtmlRenderContext} JS对象。
	 * <p>
	 * 如果设置为{@code true}，那么必须设置{@linkplain #setRenderContextVarName(String)}。
	 * </p>
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
	 * 自定义是否不输出调用渲染图表的函数（类似：<code>chartPlugin.render(chart);</code>）。
	 * 
	 * @param notWriteInvoke
	 */
	public void setNotWriteInvoke(boolean notWriteInvoke)
	{
		this.notWriteInvoke = notWriteInvoke;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [chartElementId=" + chartElementId + ", notWriteChartElement="
				+ notWriteChartElement + ", pluginVarName=" + pluginVarName + ", notWritePluginObject="
				+ notWritePluginObject + ", chartVarName=" + chartVarName + ", renderContextVarName="
				+ renderContextVarName + ", notWriteRenderContextObject=" + notWriteRenderContextObject
				+ ", notWriteScriptTag=" + notWriteScriptTag + ", notWriteInvoke=" + notWriteInvoke + "]";
	}

	/**
	 * 设置{@linkplain HtmlChartPluginRenderOption}对象。
	 * 
	 * @param renderContext
	 * @param option
	 */
	public static void setOption(RenderContext renderContext, HtmlChartPluginRenderOption option)
	{
		renderContext.setAttribute(ATTR_NAME, option);
	}

	/**
	 * 获取{@linkplain HtmlChartPluginRenderOption}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartPluginRenderOption getOption(RenderContext renderContext)
	{
		return renderContext.getAttribute(ATTR_NAME);
	}

	/**
	 * 移除{@linkplain HtmlChartPluginRenderOption}对象。
	 * 
	 * @param renderContext
	 */
	public static HtmlChartPluginRenderOption removeOption(RenderContext renderContext)
	{
		return renderContext.removeAttribute(ATTR_NAME);
	}

	public static final String ATTR_NAME = HtmlChartPluginRenderOption.class.getName();
}
