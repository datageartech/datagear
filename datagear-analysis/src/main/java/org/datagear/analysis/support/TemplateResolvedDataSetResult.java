/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;

/**
 * 模板解析的{@linkplain ResolvedDataSetResult}。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateResolvedDataSetResult extends ResolvedDataSetResult
{
	/** 已解析的模板 */
	private String templateResult;

	public TemplateResolvedDataSetResult()
	{
		super();
	}

	public TemplateResolvedDataSetResult(DataSetResult result, List<DataSetProperty> properties, String templateResult)
	{
		super(result, properties);
		this.templateResult = templateResult;
	}

	public String getTemplateResult()
	{
		return templateResult;
	}

	public void setTemplateResult(String templateResult)
	{
		this.templateResult = templateResult;
	}
}
