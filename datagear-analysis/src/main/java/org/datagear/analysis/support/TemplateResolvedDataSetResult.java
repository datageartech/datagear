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
	private static final long serialVersionUID = 1L;

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
