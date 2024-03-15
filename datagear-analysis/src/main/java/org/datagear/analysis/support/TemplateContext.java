/*
 * Copyright 2018-present datagear.tech
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
