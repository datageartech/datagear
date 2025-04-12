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

package org.datagear.analysis.support.datasettpl;

import java.io.Serializable;

/**
 * 模板解析结果。
 * 
 * @author datagear@163.com
 *
 */
public class TemplateResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String result = "";

	public TemplateResult()
	{
		super();
	}

	public TemplateResult(String result)
	{
		super();
		this.result = result;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [result=" + result + "]";
	}
}
