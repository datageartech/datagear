/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
