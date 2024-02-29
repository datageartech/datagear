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

package org.datagear.meta;

import java.io.Serializable;

/**
 * 抽象表元信息。
 * 
 * @author datagear@163.com
 *
 */
public class AbstractTable implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 类型 */
	private String type;

	/** 描述 */
	private String comment;

	public AbstractTable()
	{
		super();
	}

	public AbstractTable(String name, String type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + ", comment=" + comment + "]";
	}
}
