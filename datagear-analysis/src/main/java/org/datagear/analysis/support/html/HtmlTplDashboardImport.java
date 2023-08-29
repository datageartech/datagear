/*
 * Copyright 2018-2023 datagear.tech
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

/**
 * HTML模板看板导入项。
 * <p>
 * 这些导入项通常被插入至HTML看板的{@code <head></head>}之间，作为页面依赖资源（JS、CSS等）加载。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardImport implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 内容 */
	private String content;

	public HtmlTplDashboardImport(String name, String content)
	{
		super();
		this.name = name;
		this.content = content;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", content=" + content + "]";
	}

	public static HtmlTplDashboardImport valueOf(String name, String content)
	{
		return new HtmlTplDashboardImport(name, content);
	}

	/**
	 * 构建{@code <link type='text/css' href='...' rel='stylesheet' />}导入条目。
	 * 
	 * @param name
	 * @param href
	 * @return
	 */
	public static HtmlTplDashboardImport valueOfLinkCss(String name, String href)
	{
		String content = "<link type=\"text/css\" href=\"" + href + "\" rel=\"stylesheet\" "
				+ HtmlTplDashboardWidgetRenderer.DASHBOARD_IMPORT_ITEM_NAME_ATTR + "=\"" + name + "\" />";

		return new HtmlTplDashboardImport(name, content);
	}

	/**
	 * 构建{@code <script type='text/javascript' src='...'></script>}导入条目。
	 * 
	 * @param name
	 * @param src
	 * @return
	 */
	public static HtmlTplDashboardImport valueOfJavaScript(String name, String src)
	{
		String content = "<script type=\"text/javascript\" src=\"" + src + "\" "
				+ HtmlTplDashboardWidgetRenderer.DASHBOARD_IMPORT_ITEM_NAME_ATTR + "=\"" + name + "\" ></script>";

		return new HtmlTplDashboardImport(name, content);
	}
}
