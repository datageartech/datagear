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

package org.datagear.analysis.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.datagear.util.FileUtil;

/**
 * 位置资源。
 * 
 * @author datagear@163.com
 *
 */
public class LocationResource
{
	/** 类路径资源前缀 */
	public static final String PREFIX_CLASSPATH = "classpath:";

	/** 文件路径资源前缀 */
	public static final String PREFIX_FILE = "file:";

	private String location;

	public LocationResource()
	{
	}

	public LocationResource(String location)
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

	/**
	 * 获取资源输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
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
		if (location.startsWith(PREFIX_FILE))
		{
			File file = getLocationFile(location);
			return new FileInputStream(file);
		}
		else if (location.startsWith(PREFIX_CLASSPATH))
		{
			location = location.substring(PREFIX_CLASSPATH.length());

			return getClass().getClassLoader().getResourceAsStream(location);
		}
		else
			throw new UnsupportedOperationException("Location [" + location + "] is not supported");
	}

	protected File getLocationFile(String fileLocation)
	{
		fileLocation = fileLocation.substring(PREFIX_FILE.length());

		File file = FileUtil.getFile(fileLocation);

		return file;
	}

	/**
	 * 是否是文件路径位置。
	 * 
	 * @param location
	 * @return
	 */
	public static boolean isFileLocation(String location)
	{
		if (location == null)
			return false;

		return location.startsWith(PREFIX_FILE);
	}

	/**
	 * 是否是类路径位置。
	 * 
	 * @param location
	 * @return
	 */
	public static boolean isClasspathLocation(String location)
	{
		if (location == null)
			return false;

		return location.startsWith(PREFIX_CLASSPATH);
	}

	/**
	 * 将文件路径转换为位置。
	 * 
	 * @param path
	 * @return
	 */
	public static String toFileLocation(String path)
	{
		return PREFIX_FILE + path;
	}

	/**
	 * 将类路径转换为位置。
	 * 
	 * @param path
	 * @return
	 */
	public static String toClasspathLocation(String path)
	{
		return PREFIX_CLASSPATH + path;
	}
}
