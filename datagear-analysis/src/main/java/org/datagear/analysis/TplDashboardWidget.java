/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */
package org.datagear.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板看板部件。
 * <p>
 * 它可在{@linkplain TplDashboardRenderContext}中渲染指定模板名称（{@linkplain #getTemplates()}）所描述的{@linkplain TplDashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TplDashboardWidget extends AbstractIdentifiable
{
	public static final String DEFAULT_TEMPLATE_ENCODING = "UTF-8";

	/** 模板名称集 */
	private String[] templates;

	private String templateEncoding = DEFAULT_TEMPLATE_ENCODING;

	public TplDashboardWidget()
	{
		super();
	}

	public TplDashboardWidget(String id, String... templates)
	{
		super(id);
		this.templates = templates;
	}

	public String[] getTemplates()
	{
		return templates;
	}

	public void setTemplates(String... templates)
	{
		this.templates = templates;
	}

	public String getTemplateEncoding()
	{
		return templateEncoding;
	}

	public void setTemplateEncoding(String templateEncoding)
	{
		this.templateEncoding = templateEncoding;
	}

	/**
	 * 获取第一个模板名称。
	 * 
	 * @return
	 * @throws IllegalStateException 当没有任何模板时抛出此异常
	 */
	public String getFirstTemplate() throws IllegalStateException
	{
		if (getTemplateCount() < 1)
			throw new IllegalStateException();

		return this.templates[0];
	}

	/**
	 * 判断是否是模板名称。
	 * 
	 * @param template 模板名称
	 * @return
	 */
	public boolean isTemplate(String template)
	{
		if (this.templates == null)
			return false;

		for (String t : this.templates)
		{
			if (t.equals(template))
				return true;
		}

		return false;
	}

	/**
	 * 获取模板名称数目。
	 * 
	 * @return
	 */
	public int getTemplateCount()
	{
		return (this.templates == null ? 0 : this.templates.length);
	}

	/**
	 * 移除指定模板名称。
	 * 
	 * @param template
	 */
	public void removeTemplate(String template)
	{
		if (this.templates == null || this.templates.length == 0)
			return;

		List<String> list = new ArrayList<>(this.templates.length);

		for (String t : this.templates)
		{
			if (!t.equals(template))
				list.add(t);
		}

		this.templates = list.toArray(new String[list.size()]);
	}

	/**
	 * 渲染指定模板的{@linkplain TplDashboard}。
	 * <p>
	 * 每次渲染的{@linkplain TplDashboard#getId()}都应全局唯一，{@linkplain TplDashboard#getCharts()}中每个{@linkplain Chart#getId()}应局部唯一。
	 * </p>
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public abstract TplDashboard render(TplDashboardRenderContext renderContext)
			throws RenderException;
}
