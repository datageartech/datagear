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

package org.datagear.management.domain;

/**
 * 抽象字符串ID实体。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractStringIdEntity extends AbstractEntity<String>
{
	private static final long serialVersionUID = 1L;

	public AbstractStringIdEntity()
	{
		super();
	}

	public AbstractStringIdEntity(String id)
	{
		super(id);
	}
}
