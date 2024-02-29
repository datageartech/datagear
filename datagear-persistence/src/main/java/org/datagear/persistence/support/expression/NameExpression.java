/*
 * Copyright 2018-2024 datagear.tech
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

package org.datagear.persistence.support.expression;

import org.datagear.util.expression.Expression;

/**
 * 名字表达式。
 * <p>
 * 格式为：<code>"${name:content}"</code>、<code>"#{name:content}"</code>
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class NameExpression extends Expression
{
	private static final long serialVersionUID = 1L;

	private String name;

	public NameExpression()
	{
		super();
	}

	public NameExpression(String startIdentifier, String endIdentifier, String expression, int startIndex, int endIndex,
			String content)
	{
		super(startIdentifier, endIdentifier, expression, startIndex, endIndex, content);
	}

	public boolean hasName()
	{
		return (this.name != null && !this.name.isEmpty());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
