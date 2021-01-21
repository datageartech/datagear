/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Map;

/**
 * 模板上下文。
 * <p>
 * 此类封装{@linkplain TemplateResolver}解析模板所需要的上下文信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TemplateContext
{
	Map<String, ?> values;

	public TemplateContext()
	{
		super();
	}

	public TemplateContext(Map<String, ?> values)
	{
		super();
		this.values = values;
	}

	public Map<String, ?> getValues()
	{
		return values;
	}

	public void setValues(Map<String, ?> values)
	{
		this.values = values;
	}
}
