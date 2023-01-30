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

package org.datagear.persistence.support.expression;

/**
 * SQL表达式解析器。
 * <p>
 * 此类将表达式格式固化为<code>${name:value}、${value}</code>，用于解析SQL表达式。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlExpressionResolver extends NameExpressionResolver
{
	public SqlExpressionResolver()
	{
		super();
		super.setStartIdentifier(DEFAULT_START_IDENTIFIER_DOLLAR);
		super.setSeparator(DEFAULT_SEPARATOR);
		super.setEndIdentifier(DEFAULT_END_IDENTIFIER);
	}

	@Override
	public void setStartIdentifier(String startIdentifier)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setEndIdentifier(String endIdentifier)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSeparator(String separator)
	{
		throw new UnsupportedOperationException();
	}
}
