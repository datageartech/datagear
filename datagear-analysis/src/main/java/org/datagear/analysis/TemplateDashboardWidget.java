/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */
package org.datagear.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.datagear.analysis.support.LocationResource;
import org.datagear.util.IOUtil;

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
{
	private String template;

	private String templateEncoding = "UTF-8";

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

	/**
	 * 渲染{@linkplain Dashboard}。
	 * 
	 * @param renderContext
	 * @return
	 * @throws RenderException
	 */
	public abstract Dashboard render(T renderContext) throws RenderException;

	/**
	 * 获取{@linkplain #getTemplate()}的输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected BufferedReader getTemplateReader() throws IOException
	{
		BufferedReader reader = null;

		if (LocationResource.isFileLocation(this.template)
				|| LocationResource.isClasspathLocation(this.template))
		{
			LocationResource resource = new LocationResource(this.template);
			reader = IOUtil.getReader(resource.getInputStream(), this.templateEncoding);
		}
		else
		{
			reader = new BufferedReader(new StringReader(this.template));
		}

		return reader;
	}
}
