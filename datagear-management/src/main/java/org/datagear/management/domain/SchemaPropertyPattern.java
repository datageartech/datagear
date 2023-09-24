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

package org.datagear.management.domain;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * 数据源连接属性匹配模式。
 * 
 * @author datagear@163.com
 *
 */
public class SchemaPropertyPattern implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 属性名匹配模式，{@code null}、{@code ""}匹配所有 */
	private String namePattern = "";

	/** 属性值匹配模式，{@code null}、{@code ""}匹配所有 */
	private String valuePattern = "";

	public SchemaPropertyPattern()
	{
		super();
	}

	public SchemaPropertyPattern(String namePattern)
	{
		super();
		this.namePattern = namePattern;
	}

	public SchemaPropertyPattern(String namePattern, String valuePattern)
	{
		super();
		this.namePattern = namePattern;
		this.valuePattern = valuePattern;
	}

	public String getNamePattern()
	{
		return namePattern;
	}

	public void setNamePattern(String namePattern)
	{
		this.namePattern = namePattern;
	}

	@Nullable
	public String getValuePattern()
	{
		return valuePattern;
	}

	public void setValuePattern(String valuePattern)
	{
		this.valuePattern = valuePattern;
	}
}
