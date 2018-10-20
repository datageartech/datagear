/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
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
