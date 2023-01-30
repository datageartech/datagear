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

package org.datagear.connection;

import java.sql.Connection;

/**
 * 常量{@linkplain ConnectionSensor}。
 * <p>
 * 此类的{@linkplain #supports(Connection)}方法始终返回{@linkplain #isSupported()}的值（默认为{@code true}）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConstantConnectionSensor implements ConnectionSensor
{
	private boolean supported = true;

	public ConstantConnectionSensor()
	{
		super();
	}

	public ConstantConnectionSensor(boolean supported)
	{
		super();
		this.supported = supported;
	}

	public boolean isSupported()
	{
		return supported;
	}

	public void setSupported(boolean supported)
	{
		this.supported = supported;
	}

	@Override
	public boolean supports(Connection cn)
	{
		return this.supported;
	}
}
