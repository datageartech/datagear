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

import java.io.Serializable;
import java.util.Arrays;

/**
 * 简单HTML模板选项。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleHtmlTplOption implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * <code>&lt;html&gt;</code>元素属性
	 */
	private String htmlAttr = "";

	/**
	 * <code>&lt;meta&gt;</code>元素编码
	 */
	private String charset = "UTF-8";

	/**
	 * <code>&lt;title&gt;</code>元素内容
	 */
	private String title = "";

	/**
	 * <code>&lt;style&gt;</code>元素内容
	 */
	private String style = "";

	/**
	 * <code>&lt;body&gt;</code>元素样式类名
	 */
	private String bodyStyleName = "";

	/**
	 * <code>&lt;body&gt;</code>元素属性
	 */
	private String bodyAttr = "";

	/**
	 * 图表ID
	 */
	private String[] chartWidgetIds = new String[0];

	/**
	 * <code>&lt;div&gt;</code>图表元素样式类名
	 */
	private String chartEleStyleName = "";

	/**
	 * <code>&lt;div&gt;</code>图表元素属性
	 */
	private String chartEleAttr = "";

	public SimpleHtmlTplOption()
	{
		super();
	}

	public SimpleHtmlTplOption(String htmlAttr, String charset, String title, String bodyStyleName, String bodyAttr,
			String[] chartWidgetIds, String chartEleStyleName, String chartEleAttr)
	{
		super();
		this.htmlAttr = htmlAttr;
		this.charset = charset;
		this.title = title;
		this.bodyStyleName = bodyStyleName;
		this.bodyAttr = bodyAttr;
		this.chartWidgetIds = chartWidgetIds;
		this.chartEleStyleName = chartEleStyleName;
		this.chartEleAttr = chartEleAttr;
	}

	public String getHtmlAttr()
	{
		return htmlAttr;
	}

	public void setHtmlAttr(String htmlAttr)
	{
		this.htmlAttr = htmlAttr;
	}

	public String getCharset()
	{
		return charset;
	}

	public void setCharset(String charset)
	{
		this.charset = charset;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}

	public String getBodyStyleName()
	{
		return bodyStyleName;
	}

	public void setBodyStyleName(String bodyStyleName)
	{
		this.bodyStyleName = bodyStyleName;
	}

	public String getBodyAttr()
	{
		return bodyAttr;
	}

	public void setBodyAttr(String bodyAttr)
	{
		this.bodyAttr = bodyAttr;
	}

	public String[] getChartWidgetIds()
	{
		return chartWidgetIds;
	}

	public void setChartWidgetIds(String[] chartWidgetIds)
	{
		this.chartWidgetIds = chartWidgetIds;
	}

	public String getChartEleStyleName()
	{
		return chartEleStyleName;
	}

	public void setChartEleStyleName(String chartEleStyleName)
	{
		this.chartEleStyleName = chartEleStyleName;
	}

	public String getChartEleAttr()
	{
		return chartEleAttr;
	}

	public void setChartEleAttr(String chartEleAttr)
	{
		this.chartEleAttr = chartEleAttr;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [htmlAttr=" + htmlAttr + ", charset=" + charset + ", title=" + title
				+ ", style=" + style + ", bodyStyleName=" + bodyStyleName + ", bodyAttr=" + bodyAttr
				+ ", chartWidgetIds=" + Arrays.toString(chartWidgetIds) + ", chartEleStyleName=" + chartEleStyleName
				+ ", chartEleAttr=" + chartEleAttr + "]";
	}
}
