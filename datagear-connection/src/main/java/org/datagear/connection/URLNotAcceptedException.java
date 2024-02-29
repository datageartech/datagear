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
 * {@linkplain DriverEntity}对应的驱动不支持给定的URL异常。
 * 
 * @author datagear@163.com
 *
 */
public class URLNotAcceptedException extends ConnectionSourceException
{
	private static final long serialVersionUID = 1L;

	private DriverEntity driverEntity;

	private String url;

	public URLNotAcceptedException(DriverEntity driverEntity, String url)
	{
		super(driverEntity + " 's driver can not accept url [" + url + "]");
		this.driverEntity = driverEntity;
		this.url = url;
	}

	public DriverEntity getDriverEntity()
	{
		return driverEntity;
	}

	public String getUrl()
	{
		return url;
	}
}
