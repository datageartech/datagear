/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.analysis.Icon;

/**
 * 位置图标。
 * 
 * @author datagear@163.com
 *
 */
public class LocationIcon implements Icon
{
	/** 类路径位置前缀 */
	public static final String LOCATION_PREFIX_CLASSPATH = "classpath:";

	/** 文件路径位置前缀 */
	public static final String LOCATION_PREFIX_FILE = "file:";

	private String location;

	public LocationIcon()
	{
	}

	public LocationIcon(String location)
	{
		super();
		this.location = location;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return getLocationInputStream(this.location);
	}

	/**
	 * 获取指定位置的输入流。
	 * 
	 * @param location
	 * @return
	 * @throws IOException
	 */
	protected InputStream getLocationInputStream(String location) throws IOException
	{
		if (location.startsWith(LOCATION_PREFIX_FILE))
		{
			location = location.substring(LOCATION_PREFIX_FILE.length());

			File file = new File(location);

			return new FileInputStream(file);
		}
		else if (location.startsWith(LOCATION_PREFIX_CLASSPATH))
		{
			location = location.substring(LOCATION_PREFIX_CLASSPATH.length());

			return getClass().getClassLoader().getResourceAsStream(location);
		}
		else
			throw new UnsupportedOperationException("Location [" + location + "] is not supported");
	}
}
