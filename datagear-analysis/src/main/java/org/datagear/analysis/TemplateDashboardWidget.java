/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */
package org.datagear.analysis;

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
public abstract class TemplateDashboardWidget<T extends RenderContext> extends AbstractIdentifiable
		implements DashboardWidget<T>
{
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
		if (this.templates == null || this.templates.length < 1)
			return null;

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

		for(String t : this.templates)
		{
			if(t.equals(template))
				return true;
		}
		
		return false;
	}

	@Override
	public TemplateDashboard render(T renderContext) throws RenderException
	{
		String template = getFirstTemplate();

		if (StringUtil.isEmpty(template))
			throw new IllegalArgumentException();

		return renderTemplate(renderContext, template);
	}

	/**
	 * 渲染指定名称模板。
	 * 
	 * @param renderContext
	 * @param template
	 * @return
	 * @throws RenderException
	 * @throws IllegalArgumentException {@code template}不是模板时
	 */
	public TemplateDashboard render(T renderContext, String template) throws RenderException, IllegalArgumentException
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
	protected abstract TemplateDashboard renderTemplate(T renderContext, String template) throws RenderException;
}
