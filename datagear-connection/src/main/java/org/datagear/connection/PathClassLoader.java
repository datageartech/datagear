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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 指定路径的类加载器。
 * <p>
 * 注意：对于非JDK自带的类，此类加载器仅会从{@linkplain #getPath()}加载，而不会代理给父类加载器。
 * </p>
 * <p>
 * 此规则可以避加载类依赖的某些库在{@linkplain #getPath()}中找不到时代理至应用类加载器，而可能出现版本不一致的情况。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PathClassLoader extends URLClassLoader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PathClassLoader.class);
	private static final boolean LOGGER_DEBUG_ENABLED = LOGGER.isDebugEnabled();

	private static final String CLASS_FILE_SUFFIX = ".class";

	private File path;

	/** 要强制加载类路径之外的类名集 */
	private Set<String> outsideForceLoads = Collections.emptySet();

	public PathClassLoader(String path)
	{
		this(FileUtil.getFile(path), null);
	}

	public PathClassLoader(File path)
	{
		this(path, null);
	}

	public PathClassLoader(String path, ClassLoader parent)
	{
		this(FileUtil.getFile(path), parent);
	}

	public PathClassLoader(File path, ClassLoader parent)
	{
		super(toLoadClassURLs(path), parent);
		this.path = path;
	}

	public File getPath()
	{
		return path;
	}

	public Set<String> getOutsideForceLoads()
	{
		return outsideForceLoads;
	}

	public void setOutsideForceLoads(Set<String> outsideForceLoads)
	{
		this.outsideForceLoads = outsideForceLoads;
	}

	@Override
	public URL getResource(String name)
	{
		// 这里不能直接使用父类方法，因为驱动程序类加载器是独立的，不应该代理至父类加载器
		URL url = findResource(name);

		// 禁用这里的调试输出，有些驱动（MySQL驱动）会频繁自动调用此接口，刷屏日志
		// if (LOGGER.isDebugEnabled())
		// LOGGER.debug("getResource [" + url + "] for [" + name + "] in path ["
		// + getPath() + "]");

		return url;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		// 这里不能直接使用父类方法，因为驱动程序类加载器是独立的，不应该代理至父类加载器
		Enumeration<URL> urls = findResources(name);

		if (LOGGER_DEBUG_ENABLED)
			LOGGER.debug("getResources for [" + name + "] in path [" + getPath() + "]");

		return urls;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		InputStream in = super.getResourceAsStream(name);

		if (LOGGER_DEBUG_ENABLED)
			LOGGER.debug("getResourceAsStream [" + in + "] for [" + name + "] in path [" + getPath() + "]");

		return in;
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException, ClassFormatError
	{
		// JDK标准库应由父类加载
		if (isJDKStandardClassName(name))
			return Class.forName(name, resolve, getParentClassLoader());

		synchronized (getClassLoadingLock(name))
		{
			Class<?> clazz = findLoadedClass(name);

			if (clazz == null)
			{
				try
				{
					clazz = findClass(name);
				}
				catch (ClassNotFoundException e)
				{
				}
				catch (ClassFormatError e)
				{
					// 类加载出错（比如版本不兼容），则抛出
					throw e;
				}
			}

			// 强制在此加载的类
			if (clazz == null && this.outsideForceLoads != null && this.outsideForceLoads.contains(name))
			{
				InputStream in = getParentClassLoader().getResourceAsStream(classNameToPath(name));

				if (in == null)
					throw new ClassNotFoundException(name);

				byte[] bytes = null;

				try
				{
					bytes = IOUtil.getBytes(in);
				}
				catch (IOException e)
				{
					throw new ClassNotFoundException(name, e);
				}
				finally
				{
					IOUtil.close(in);
				}

				clazz = defineClass(name, bytes, 0, bytes.length);
			}

			// 此时仅代理父类加载JDK扩展库，其他都应由此类加载器加载
			if (clazz == null && isJDKExtClassName(name))
			{
				ClassLoader parent = getParentClassLoader();

				if (LOGGER_DEBUG_ENABLED)
					LOGGER.debug("delegate parent class loader for loading class [" + name + "]");

				clazz = Class.forName(name, false, parent);
			}

			if (clazz == null)
				throw new ClassNotFoundException(name);

			if (resolve)
				resolveClass(clazz);

			if (LOGGER_DEBUG_ENABLED)
				LOGGER.debug("load class [" + (clazz == null ? "null" : clazz.getName()) + "] for name [" + name
						+ "] in path [" + getPath() + "]");

			return clazz;
		}
	}

	/**
	 * 是否JDK标准类名。
	 * 
	 * @param name
	 * @return
	 */
	protected boolean isJDKStandardClassName(String name)
	{
		return (name.startsWith("java.") || name.startsWith("javax."));
	}

	/**
	 * 是否JDK扩展类名。
	 * 
	 * @param name
	 * @return
	 */
	protected boolean isJDKExtClassName(String name)
	{
		return (name.startsWith("sun.") || name.startsWith("org.ietf.") || name.startsWith("org.omg.")
				|| name.startsWith("org.w3c.") || name.startsWith("org.xml.") || name.startsWith("jdk."));
	}

	/**
	 * 获取上级类加载器。
	 * 
	 * @return
	 */
	protected ClassLoader getParentClassLoader()
	{
		ClassLoader parent = getParent();
		if (parent == null)
			parent = PathClassLoader.class.getClassLoader();

		return parent;
	}

	/**
	 * 将类名转换为路径名。
	 * 
	 * @param className
	 * @return
	 */
	protected String classNameToPath(String className)
	{
		StringBuilder path = new StringBuilder();

		path.append(className.replace('.', '/'));
		path.append(CLASS_FILE_SUFFIX);

		return path.toString();
	}

	protected static URL[] toLoadClassURLs(File path)
	{
		if (path.isDirectory())
		{
			File[] children = path.listFiles();

			List<URL> urls = new ArrayList<URL>();

			urls.add(FileUtil.toURL(path));

			for (int i = 0; i < children.length; i++)
			{
				File child = children[i];

				if (child.isDirectory())
					;
				else
					urls.add(FileUtil.toURL(child));
			}

			return urls.toArray(new URL[urls.size()]);
		}
		else
			return new URL[] { FileUtil.toURL(path) };
	}
}
