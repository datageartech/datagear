/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
