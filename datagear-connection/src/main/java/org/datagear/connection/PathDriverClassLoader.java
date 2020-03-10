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
 * 注意：对于非JDK自带的类，此类加载器仅会从{@linkplain #getPath()}加载，而不会代理给父类加载器。
 * </p>
 * <p>
 * 此规则可以避免驱动类依赖的某些库在{@linkplain #getPath()}中找不到时代理至应用类加载器，而可能出现版本不一致的情况。
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
		URL url = findResource(name);

		// 对于独立的驱动程序类加载器，不应该代理至父类加载器
//		if (url == null)
//		{
//			ClassLoader parent = getParentClassLoader();
//
//			if (LOGGER.isDebugEnabled())
//				LOGGER.debug("delegate parent class loader for getting resource URL for [" + name + "]");
//
//			return parent.getResource(name);
//		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("getResource [" + url + "] for [" + name + "] in path [" + getPath() + "]");

		return url;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		Enumeration<URL> urls = findResources(name);

		// 对于独立的驱动程序类加载器，不应该代理至父类加载器
//		if (urls == null || urls.hasMoreElements())
//		{
//			ClassLoader parent = getParentClassLoader();
//
//			if (LOGGER.isDebugEnabled())
//				LOGGER.debug("delegate parent class loader for getting resource URLs for [" + name + "]");
//
//			return parent.getResources(name);
//		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("getResources for [" + name + "] in path [" + getPath() + "]");

		return urls;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		URL url = findResource(name);

		// 对于独立的驱动程序类加载器，不应该代理至父类加载器
//		if (url == null)
//		{
//			ClassLoader parent = getParentClassLoader();
//
//			if (LOGGER.isDebugEnabled())
//				LOGGER.debug("delegate parent class loader for getting resource as stream for [" + name + "]");
//
//			return parent.getResourceAsStream(name);
//		}

		if (url == null)
			return null;

		InputStream in = null;

		try
		{
			in = url.openStream();
		}
		catch (IOException e)
		{
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("getResourceAsStream [" + in + "] for [" + name + "] in path [" + getPath() + "]");

		return in;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException, ClassFormatError
	{
		// JDK标准库应由父类加载
		if (isJDKStandardClassName(name))
			return Class.forName(name, resolve, getParentClassLoader());

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
			try
			{
				clazz = findClass(name);
			}
			catch(ClassNotFoundException e)
			{
			}
			catch(ClassFormatError e)
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
			catch(IOException e)
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

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for loading class [" + name + "]");

			clazz = Class.forName(name, false, parent);
		}

		if (resolve)
			resolveClass(clazz);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("load class [" + (clazz == null ? "null" : clazz.getName()) + "] for name [" + name
					+ "] in path [" + getPath() + "]");

		return clazz;
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
				|| name.startsWith("org.w3c.") || name.startsWith("org.xml."));
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
