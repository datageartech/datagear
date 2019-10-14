/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.connection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 指定路径的驱动类加载器。
 * <p>
 * 注意：此类会优先从指定路径中加载类，因此，不应该将标准库放入此路径中。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PathDriverClassLoader extends URLClassLoader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PathDriverClassLoader.class);

	private static final String CLASS_FILE_SUFFIX = ".class";

	private File path;

	/** 要强制加载类路径之外的类名集 */
	private Set<String> outsideForceLoads = new HashSet<String>();

	public PathDriverClassLoader(String path)
	{
		this(new File(path), null);
	}

	public PathDriverClassLoader(File path)
	{
		this(path, null);
	}

	public PathDriverClassLoader(String path, ClassLoader parent)
	{
		this(new File(path), parent);
	}

	public PathDriverClassLoader(File path, ClassLoader parent)
	{
		super(toLoadClassURLs(path), parent);
		this.path = path;
		this.outsideForceLoads.add(DriverTool.class.getName());
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
		this.outsideForceLoads.addAll(outsideForceLoads);
	}

	@Override
	public URL getResource(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URL for [" + name + "]");

		URL url = findResource(name);

		if (url == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URL for [" + name + "]");

			return parent.getResource(name);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("got resource URL [" + url + "] for [" + name + "] in path [" + getPath() + "]");

		return url;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URLs for [" + name + "]");

		Enumeration<URL> urls = findResources(name);

		if (urls == null || urls.hasMoreElements())
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URLs for [" + name + "]");

			return parent.getResources(name);
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("got resource URLs for [" + name + "] in path [" + getPath() + "]");

		return urls;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource as stream for [" + name + "]");

		URL url = findResource(name);

		if (url == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource as stream for [" + name + "]");

			return parent.getResourceAsStream(name);
		}

		try
		{
			return url.openStream();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException, ClassFormatError
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start loading class [" + name + "]");

		// -拷贝自java.net.URLClassLoader.FactoryURLClassLoader
		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
		{
			int i = name.lastIndexOf('.');
			if (i != -1)
			{
				sm.checkPackageAccess(name.substring(0, i));
			}
		}
		// -

		Class<?> clazz = findLoadedClass(name);

		if (clazz == null)
		{
			// 标准库不应在此加载
			if (name.startsWith("java"))
				;
			else
			{
				try
				{
					clazz = findClass(name);
				}
				catch (ClassNotFoundException e)
				{
					// 找不到类，则代理给父类加载器
				}
				catch (ClassFormatError e)
				{
					// 类加载出错（比如版本不兼容），则抛出
					throw e;
				}
			}
		}

		if (clazz == null)
		{
			// 强制在此加载的类
			if (this.outsideForceLoads != null && this.outsideForceLoads.contains(name))
			{
				InputStream in = getResourceAsStream(classNameToPath(name));

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
		}

		if (clazz == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for loading class [" + name + "]");

			clazz = Class.forName(name, false, parent);
		}

		if (resolve)
			resolveClass(clazz);

		return clazz;
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
			parent = PathDriverClassLoader.class.getClassLoader();

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
