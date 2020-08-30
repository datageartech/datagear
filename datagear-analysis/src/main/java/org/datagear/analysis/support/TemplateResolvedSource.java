/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

/**
 * 模板已解析的源。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateResolvedSource<T>
{
	private T source;

	/** 已解析的模板内容 */
	private String resolvedTemplate = null;

	public TemplateResolvedSource()
	{
		super();
	}

	public TemplateResolvedSource(T source)
	{
		super();
		this.source = source;
	}

	public TemplateResolvedSource(T source, String resolvedTemplate)
	{
		super();
		this.source = source;
		this.resolvedTemplate = resolvedTemplate;
	}

	public T getSource()
	{
		return source;
	}

	public void setSource(T source)
	{
		this.source = source;
	}

	public boolean hasResolvedTemplate()
	{
		return (this.resolvedTemplate != null && !this.resolvedTemplate.isEmpty());
	}

	public String getResolvedTemplate()
	{
		return resolvedTemplate;
	}

	public void setResolvedTemplate(String resolvedTemplate)
	{
		this.resolvedTemplate = resolvedTemplate;
	}
}
