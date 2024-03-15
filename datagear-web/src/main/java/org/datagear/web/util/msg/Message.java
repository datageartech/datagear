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

package org.datagear.web.util.msg;

import java.io.Serializable;

/**
 * 消息。
 * 
 * @author datagear@163.com
 *
 */
public class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String type;

	public Message()
	{
		super();
		this.type = getClass().getSimpleName();
	}

	public String getType()
	{
		return type;
	}

	protected void setType(String type)
	{
		this.type = type;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [type=" + type + "]";
	}
}
