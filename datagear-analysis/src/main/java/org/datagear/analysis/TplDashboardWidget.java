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
package org.datagear.analysis;

import java.util.ArrayList;
import java.util.List;

import org.datagear.util.IOUtil;

/**
 * 模板看板部件。
 * <p>
 * 它可以渲染指定{@linkplain TplDashboardRenderContext}的{@linkplain TplDashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TplDashboardWidget extends AbstractIdentifiable
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_TEMPLATE_ENCODING = IOUtil.CHARSET_UTF_8;

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
	 * 渲染{@linkplain TplDashboard}。
	 * <p>
	 * 如果参数的{@linkplain TplDashboardRenderContext#getTemplateReader()}不为{@code null}，那么实现类不应在渲染完成后关闭其输入流。
	 * </p>
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
