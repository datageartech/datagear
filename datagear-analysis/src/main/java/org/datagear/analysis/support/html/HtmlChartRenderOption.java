/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Serializable;

import org.datagear.analysis.RenderContext;
import org.datagear.util.Global;

/**
 * {@linkplain HtmlChart}渲染选项。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartRenderOption implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 图表的HTML元素ID */
	private String chartElementId = genIdentifier("ele");

	/** 图表变量名 */
	private String chartVarName = genIdentifier("Chart");

	/** 插件变量名 */
	private String pluginVarName = genIdentifier("Plugin");

	/** 渲染上下文变量名 */
	private String renderContextVarName = genIdentifier("RenderContext");

	/** 是否不输出图表HTML元素 */
	private boolean notWriteChartElement = false;

	/** 是否不输出插件JS对象 */
	private boolean notWritePluginObject = false;

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
		return getClass().getSimpleName() + " [chartElementId=" + chartElementId + ", chartVarName=" + chartVarName
				+ ", pluginVarName=" + pluginVarName + ", renderContextVarName=" + renderContextVarName
				+ ", notWriteChartElement=" + notWriteChartElement + ", notWritePluginObject=" + notWritePluginObject
				+ ", notWriteRenderContextObject=" + notWriteRenderContextObject + ", notWriteScriptTag="
				+ notWriteScriptTag + ", notWriteInvoke=" + notWriteInvoke + ", writeChartJson=" + writeChartJson + "]";
	}

	/**
	 * 生成标识。
	 * 
	 * @param suffix
	 * @return
	 */
	protected static String genIdentifier(String suffix)
	{
		return Global.PRODUCT_NAME_EN_LC + suffix;
	}
}
