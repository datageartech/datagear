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

package org.datagear.connection;

/**
 * 常量{@linkplain URLSensor}。
 * <p>
 * 此类的{@linkplain #supports(String)}方法始终返回{@linkplain #isSupported()}的值（默认为{@code true}）。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ConstantURLSensor implements URLSensor
{
	private boolean supported = true;

	public ConstantURLSensor()
	{
		super();
	}

	public ConstantURLSensor(boolean supported)
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
	public boolean supports(String url)
	{
		return this.supported;
	}
}
