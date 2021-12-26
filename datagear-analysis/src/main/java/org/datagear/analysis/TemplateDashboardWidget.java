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

import org.datagear.util.StringUtil;

/**
 * 模板看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己的模板（{@linkplain #getTemplates()}）所描述的{@linkplain Dashboard}。
 * </p>
 * <p>
 * 此类的{@linkplain #render(RenderContext)}渲染{@linkplain #getFirstTemplate()}模板。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TemplateDashboardWidget extends AbstractIdentifiable implements DashboardWidget
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_TEMPLATE_ENCODING = "UTF-8";

	private String[] templates;

	private String templateEncoding = DEFAULT_TEMPLATE_ENCODING;

	public TemplateDashboardWidget()
	{
		super();
	}

	public TemplateDashboardWidget(String id, String... templates)
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
	 * 获取第一个模板。
	 * 
	 * @return
	 */
	public String getFirstTemplate()
	{
		if (getTemplateCount() < 1)
			throw new IllegalStateException();

		return this.templates[0];
	}

	/**
	 * 判断是否是模板。
	 * 
	 * @param template
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
	 * 获取模板数目。
	 * 
	 * @return
	 */
	public int getTemplateCount()
	{
		return (this.templates == null ? 0 : this.templates.length);
	}

	/**
	 * 移除指定模板。
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
	 * 渲染{@linkplain #getFirstTemplate()}模板所表示的{@linkplain TemplateDashboard}。
	 */
	@Override
	public TemplateDashboard render(RenderContext renderContext) throws RenderException
	{
		String template = getFirstTemplate();

		if (StringUtil.isEmpty(template))
			throw new IllegalArgumentException();

		return renderTemplate(renderContext, template);
	}

	/**
	 * 渲染{@linkplain #getFirstTemplate()}模板所表示的{@linkplain TemplateDashboard}。
	 * 
	 * @param renderContext
	 * @param templateIn    {@linkplain #getFirstTemplate()}模板的输入流
	 * @return
	 * @throws RenderException
	 */
	public TemplateDashboard render(RenderContext renderContext, Reader templateIn) throws RenderException
	{
		String template = getFirstTemplate();

		if (StringUtil.isEmpty(template))
			throw new IllegalArgumentException();

		return renderTemplate(renderContext, template, templateIn);
	}

	/**
	 * 渲染指定名称模板所表示的{@linkplain TemplateDashboard}。
	 * 
	 * @param renderContext
	 * @param template
	 * @param templateIn    {@code template}模板的输入流
	 * @return
	 * @throws RenderException
	 * @throws IllegalArgumentException {@code template}不是模板时
	 */
	public TemplateDashboard render(RenderContext renderContext, String template, Reader templateIn)
			throws RenderException, IllegalArgumentException
	{
		if (!isTemplate(template))
			throw new IllegalArgumentException("[" + template + "] is not template");

		return renderTemplate(renderContext, template, templateIn);
	}

	/**
	 * 渲染指定名称模板所表示的{@linkplain TemplateDashboard}。
	 * 
	 * @param renderContext
	 * @param template
	 * @return
	 * @throws RenderException
	 * @throws IllegalArgumentException {@code template}不是模板时
	 */
	public TemplateDashboard render(RenderContext renderContext, String template)
			throws RenderException, IllegalArgumentException
	{
		if (!isTemplate(template))
			throw new IllegalArgumentException("[" + template + "] is not template");

		return renderTemplate(renderContext, template);
	}


	/**
	 * 渲染指定名称模板。
	 * 
	 * @param renderContext
	 * @param template
	 * @return
	 * @throws RenderException
	 */
	protected abstract TemplateDashboard renderTemplate(RenderContext renderContext, String template)
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
	protected abstract TemplateDashboard renderTemplate(RenderContext renderContext, String template, Reader templateIn)
			throws RenderException;
}
