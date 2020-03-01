/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support.html;

import java.io.Serializable;

/**
 * HTML看板导入项。
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

	public HtmlTplDashboardImport()
	{
		super();
	}

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
}
