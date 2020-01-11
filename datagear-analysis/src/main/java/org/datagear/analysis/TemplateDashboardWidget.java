/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */
package org.datagear.analysis;

/**
 * 模板看板部件。
 * <p>
 * 它可在{@linkplain RenderContext}中渲染自己的模板（{@linkplain #getTemplate()}）所描述的{@linkplain Dashboard}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class TemplateDashboardWidget<T extends RenderContext> extends AbstractIdentifiable
		implements DashboardWidget<T>
{
	public static final String DEFAULT_TEMPLATE_ENCODING = "UTF-8";

	private String template;

	private String templateEncoding = DEFAULT_TEMPLATE_ENCODING;

	public TemplateDashboardWidget()
	{
		super();
	}

	public TemplateDashboardWidget(String id, String template)
	{
		super(id);
		this.template = template;
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	public String getTemplateEncoding()
	{
		return templateEncoding;
	}

	public void setTemplateEncoding(String templateEncoding)
	{
		this.templateEncoding = templateEncoding;
	}
}
