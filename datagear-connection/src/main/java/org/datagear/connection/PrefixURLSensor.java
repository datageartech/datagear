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

/**
 * 以前缀判断的{@linkplain URLSensor}。
 * 
 * @author datagear@163.com
 *
 */
public class PrefixURLSensor implements URLSensor
{
	private String prefix;

	public PrefixURLSensor()
	{
		super();
	}

	public PrefixURLSensor(String prefix)
	{
		super();
		this.prefix = prefix;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	@Override
	public boolean supports(String url)
	{
		return url.startsWith(this.prefix);
	}
}
