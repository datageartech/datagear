/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */
package org.datagear.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染其模板名称（{@linkplain #getTemplates()}）所描述的{@linkplain Dashboard}。
 * </p>
 * <p>
 * 此类的{@linkplain #render(RenderContext)}渲染{@linkplain #getFirstTemplate()}模板。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TplDashboardWidget extends AbstractIdentifiable implements DashboardWidget
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
	 * 渲染{@linkplain #getFirstTemplate()}的{@linkplain TplDashboard}。
	 */
	@Override
	public TplDashboard render(RenderContext renderContext) throws RenderException
	{
		String template = getFirstTemplate();
		return renderTemplate(renderContext, template);
	}

	/**
	 * 渲染指定模板名称的{@linkplain TplDashboard}。
	 * 
	 * @param renderContext
	 * @param template      模板名称，应是{@linkplain #isTemplate(String)}为{@code true}
	 * @return
	 * @throws RenderException
	 * @throws IllegalArgumentException {@code template}不是模板名称时
	 */
	public TplDashboard render(RenderContext renderContext, String template)
			throws RenderException, IllegalArgumentException
	{
		if (!isTemplate(template))
			throw new IllegalArgumentException("[" + template + "] is not template");

		return renderTemplate(renderContext, template);
	}

	/**
	 * 渲染指定模板名称的{@linkplain TplDashboard}。
	 * <p>
	 * 模板名称不必是{@linkplain #isTemplate(String)}为{@code true}的，通常用于支持渲染即时看板。
	 * </p>
	 * 
	 * @param renderContext
	 * @param template      模板名称，{@linkplain #isTemplate(String)}不必为{@code true}
	 * @param templateIn    {@code template}模板的输入流
	 * @return
	 * @throws RenderException
	 */
	public TplDashboard render(RenderContext renderContext, String template, Reader templateIn)
			throws RenderException
	{
		return renderTemplate(renderContext, template, templateIn);
	}

	/**
	 * 渲染指定名称模板。
	 * 
	 * @param renderContext
	 * @param template
	 * @return
	 * @throws RenderException
	 */
	protected abstract TplDashboard renderTemplate(RenderContext renderContext, String template)
			throws RenderException;

	/**
	 * 渲染指定名称模板。
	 * 
	 * @param renderContext
	 * @param template
	 * @param templateIn
	 * @return
	 * @throws RenderException
	 */
	protected abstract TplDashboard renderTemplate(RenderContext renderContext, String template, Reader templateIn)
			throws RenderException;
}
