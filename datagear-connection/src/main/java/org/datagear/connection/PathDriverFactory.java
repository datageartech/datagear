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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;
import java.util.List;

import org.datagear.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 特定路径JDBC驱动工厂。
 * <p>
 * 此类实例在使用前需要调用其{@linkplain #init()}方法，在弃用前，需要调用其{@linkplain #release()}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PathDriverFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PathDriverFactory.class);

	/** 驱动程序库路径 */
	private File path;

	private PathDriverClassLoader pathDriverClassLoader;

	private Object driverTool;

	public PathDriverFactory(String path)
	{
		this(FileUtil.getFile(path));
	}

	public PathDriverFactory(File path)
	{
		super();
		this.path = path;
	}

	public File getPath()
	{
		return path;
	}

	/**
	 * 初始化。
	 * 
	 * @throws PathDriverFactoryException
	 */
	public synchronized void init() throws PathDriverFactoryException
	{
		if (this.pathDriverClassLoader != null)
			return;

		this.pathDriverClassLoader = initPathClassLoader(this.path);

		try
		{
			Class<?> driverToolClass = this.pathDriverClassLoader.loadClass(DriverTool.class.getName());
			this.driverTool = driverToolClass.newInstance();
		}
		catch (ClassNotFoundException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (InstantiationException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new PathDriverFactoryException(e);
		}
	}

	/**
	 * 释放资源。
	 * <p>
	 * 此方法不会抛出任何异常。
	 * </p>
	 */
	public synchronized void release()
	{
		try
		{
			releaseJdbcDrivers();
		}
		catch (Throwable t)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error("release error", t);
		}

		try
		{
			this.pathDriverClassLoader.close();
		}
		catch (Throwable t)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error("release error", t);
		}
	}

	/**
	 * 初始化{@linkplain PathDriverClassLoader}。
	 * 
	 * @param path
	 * @return
	 */
	protected PathDriverClassLoader initPathClassLoader(File path)
	{
		PathDriverClassLoader classLoader = new PathDriverClassLoader(path);

		return classLoader;
	}

	/**
	 * 获取路径上次修改时间。
	 * 
	 * @return
	 */
	public long getLastModified()
	{
		return FileUtil.lastModifiedOfPath(this.path);
	}

	/**
	 * 获取指定类名的JDBC驱动程序。
	 * 
	 * @param driverClassName
	 * @return
	 * @throws PathDriverFactoryException
	 */
	public synchronized Driver getDriver(String driverClassName) throws PathDriverFactoryException
	{
		try
		{
			Class.forName(driverClassName, true, this.pathDriverClassLoader);
		}
		catch (ClassNotFoundException e)
		{
			throw new DriverNotFoundException(this.path.getPath(), driverClassName, e);
		}
		catch (ClassFormatError e)
		{
			throw new DriverClassFormatErrorException(e);
		}
		catch (Throwable t)
		{
			throw new PathDriverFactoryException(t);
		}

		try
		{
			Driver driver = (Driver) this.driverTool.getClass().getMethod("getDriver", String.class)
					.invoke(this.driverTool, driverClassName);

			if (driver == null)
				throw new PathDriverFactoryException(
						"No Driver named [" + driverClassName + "] found in [" + this.path + "]");

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Get JDBC driver [" + driverClassName + "] in path [" + this.path + "]");

			return driver;
		}
		catch (IllegalArgumentException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (SecurityException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new PathDriverFactoryException(e);
		}
	}

	/**
	 * 释放{@linkplain Driver}资源。
	 * 
	 * @throws PathDriverFactoryException
	 */
	protected void releaseJdbcDrivers() throws PathDriverFactoryException
	{
		try
		{
			@SuppressWarnings("unchecked")
			List<String> driverNames = (List<String>) this.driverTool.getClass().getMethod("deregisterDrivers")
					.invoke(this.driverTool);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("deregister JDBC drivers loaded in path [" + this.path + "] :" + driverNames);
		}
		catch (IllegalArgumentException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (SecurityException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new PathDriverFactoryException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new PathDriverFactoryException(e);
		}
	}

	protected PathDriverClassLoader getPathDriverClassLoader()
	{
		return this.pathDriverClassLoader;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [path=" + path + "]";
	}
}
