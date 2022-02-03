/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
