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

import java.util.Collections;
import java.util.List;

/**
 * SQL 模板结果。
 * 
 * @author datagear@163.com
 *
 */
public class SqlTemplateResult extends TemplateResult
{
	private static final long serialVersionUID = 1L;

	/** 是否包含预编译语法 */
	private boolean precompiled = false;

	/** 预编译参数值列表 */
	private List<Object> paramValues = Collections.emptyList();

	public SqlTemplateResult()
	{
		super();
	}

	public SqlTemplateResult(String result, boolean precompiled)
	{
		super(result);
		this.precompiled = precompiled;
	}

	public SqlTemplateResult(String result, boolean precompiled, List<Object> paramValues)
	{
		super(result);
		this.precompiled = precompiled;
		this.paramValues = paramValues;
	}

	public boolean isPrecompiled()
	{
		return precompiled;
	}

	public void setPrecompiled(boolean precompiled)
	{
		this.precompiled = precompiled;
	}

	public List<Object> getParamValues()
	{
		return paramValues;
	}

	public void setParamValues(List<Object> paramValues)
	{
		this.paramValues = paramValues;
	}
}
