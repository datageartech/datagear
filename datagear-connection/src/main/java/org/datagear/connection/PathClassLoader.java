/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.connection;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 特定路径类加载器。
 * <p>
 * 此类实例在使用前需要调用其{@linkplain #init()}方法，在弃用前，需要调用其{@linkplain #close()}方法。
 * </p>
 * <p>
 * 注意：此类会优先从{@linkplain #getPath()}路径中加载类，因此，不应该将标准库放入此路径中。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class PathClassLoader extends ClassLoader implements Closeable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PathClassLoader.class);

	private static final String CLASS_FILE_SUFFIX = ".class";

	/** 类路径 */
	private File path;

	/** 要强制加载类路径之外的类名套集 */
	private Set<String> outsideForceLoads;

	/** 是否一直持有路径下的JAR文件，开启可以提高类加载效率 */
	private boolean holdJarFile = false;

	/** 此目录中的jar文件列表 */
	private transient List<JarFileHolder> jarFileHolders = new ArrayList<JarFileHolder>();

	public PathClassLoader(String path)
	{
		this(new File(path), null);
	}

	public PathClassLoader(File path)
	{
		this(path, null);
	}

	public PathClassLoader(String path, ClassLoader parent)
	{
		this(new File(path), parent);
	}

	public PathClassLoader(File path, ClassLoader parent)
	{
		super(parent);
		this.path = path;
	}

	public File getPath()
	{
		return this.path;
	}

	public void setPath(File path)
	{
		this.path = path;
	}

	public Set<String> getOutsideForceLoads()
	{
		return outsideForceLoads;
	}

	public void setOutsideForceLoads(Set<String> outsideForceLoads)
	{
		this.outsideForceLoads = outsideForceLoads;
	}

	public void setOutsideForceLoads(String... outsideForceLoads)
	{
		this.outsideForceLoads = new HashSet<String>();

		for (String outsideForceLoad : outsideForceLoads)
			this.outsideForceLoads.add(outsideForceLoad);
	}

	/**
	 * 是否持有路径下的JAR文件。
	 * 
	 * @return
	 */
	public boolean isHoldJarFile()
	{
		return holdJarFile;
	}

	/**
	 * 设置是否持有路径下的所有JAR文件。
	 * <p>
	 * 如果设置为{@code true}，那么在{@linkplain #init()}中将初始化所有JAR文件，这样可以提高类加载效率，但是会一直占用JAR文件，导致其无法被修改。
	 * </p>
	 * <p>
	 * 如果设置为{@code false}，在{@linkplain #init()}不会初始化JAR文件，而是在每次加载类的时候即时访问，这样会降低类加载效率，但是不会占用JAR文件，使其可被编辑。
	 * </p>
	 * <p>
	 * 此项的默认值为{@code false}。
	 * </p>
	 * 
	 * @param holdJarFile
	 */
	public void setHoldJarFile(boolean holdJarFile)
	{
		this.holdJarFile = holdJarFile;
	}

	/**
	 * 初始化。
	 */
	public void init()
	{
		if (this.holdJarFile)
		{
			this.jarFileHolders.clear();
			findJarFileHolders(this.path, this.jarFileHolders, false);
		}
	}

	/**
	 * 关闭。
	 * <p>
	 * 关闭将释放占用资源，并使之不再可用。
	 * </p>
	 */
	@Override
	public void close()
	{
		for (JarFileHolder jarFileHolder : this.jarFileHolders)
		{
			JarFileHolder tmpJarFileHolder = new JarFileHolder(jarFileHolder.getJarFile(), jarFileHolder.getFile(),
					true);

			IOUtil.close(tmpJarFileHolder);
		}
	}

	@Override
	public URL getResource(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URL for [" + name + "]");

		ResourceHolder resourceHolder = findResourceHolder(name);

		if (resourceHolder == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URL for [" + name + "]");

			return parent.getResource(name);
		}

		try
		{
			URL url = getResourceURL(resourceHolder);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("got resource URL [" + url + "] for [" + name + "] in file ["
						+ resourceHolder.getFile().getPath() + "]");

			return url;
		}
		catch (IOException e)
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("getting resource URL for [" + name + "] in file [" + resourceHolder.getFile().getPath()
						+ "] error", e);
		}
		finally
		{
			IOUtil.close(resourceHolder);
		}

		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URLs for [" + name + "]");

		List<ResourceHolder> resourceHolders = findResourceHolders(name);

		if (resourceHolders == null || resourceHolders.isEmpty())
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URLs for [" + name + "]");

			return parent.getResources(name);
		}

		try
		{
			Vector<URL> urls = new Vector<URL>();

			for (ResourceHolder resourceHolder : resourceHolders)
			{
				URL url = getResourceURL(resourceHolder);
				urls.add(url);
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("got resource URLs for [" + name + "] in path [" + this.path.getPath() + "]");

			return urls.elements();
		}
		catch (IOException e)
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("getting resource URLs for [" + name + "] in path [" + this.path.getPath() + "] error", e);
		}
		finally
		{
			for (ResourceHolder resourceHolder : resourceHolders)
				IOUtil.close(resourceHolder);
		}

		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource as stream for [" + name + "]");

		ResourceHolder resourceHolder = findResourceHolder(name);

		if (resourceHolder == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource as stream for [" + name + "]");

			return parent.getResourceAsStream(name);
		}

		try
		{
			byte[] bytes = getResourceBytes(resourceHolder);

			InputStream in = new ByteArrayInputStream(bytes);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("got resource as stream for [" + name + "] in file [" + resourceHolder.getFile().getPath()
						+ "]");

			return in;
		}
		catch (IOException e)
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("getting resource as stream for [" + name + "] in file ["
						+ resourceHolder.getFile().getPath() + "] error", e);
		}
		finally
		{
			IOUtil.close(resourceHolder);
		}

		return null;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException, ClassFormatError
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start loading class [" + name + "]");

		Class<?> clazz = findLoadedClass(name);

		if (clazz == null)
		{
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

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException, ClassFormatError
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start finding class [" + name + "]");

		String classFilePath = classNameToPath(name);

		ResourceHolder classResourceHolder = findResourceHolder(classFilePath);

		if (classResourceHolder == null)
			throw new ClassNotFoundException(name);

		byte[] classBytes = null;

		try
		{
			classBytes = getResourceBytes(classResourceHolder);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(
						"found class for [" + name + "] in file [" + classResourceHolder.getFile().getPath() + "]");
		}
		catch (IOException e)
		{
			throw new ClassNotFoundException(name, e);
		}
		finally
		{
			IOUtil.close(classResourceHolder);
		}

		Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);

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
			parent = PathClassLoader.class.getClassLoader();

		return parent;
	}

	/**
	 * 获取{@linkplain ResourceHolder}的{@linkplain URL}。
	 * 
	 * @param resourceHolder
	 * @return
	 * @throws IOException
	 */
	protected URL getResourceURL(ResourceHolder resourceHolder) throws IOException
	{
		if (resourceHolder.isJarEntity())
		{
			StringBuilder sb = new StringBuilder("jar:");
			sb.append(resourceHolder.getFile().toURI().toURL().toString());
			sb.append("!/");
			sb.append(resourceHolder.getJarEntry().getName());

			return new URL(sb.toString());
		}
		else
			return resourceHolder.getFile().toURI().toURL();
	}

	/**
	 * 获取{@linkplain ResourceHolder}的字节数组。
	 * 
	 * @param resourceHolder
	 * @return
	 * @throws IOException
	 */
	protected byte[] getResourceBytes(ResourceHolder resourceHolder) throws IOException
	{
		if (resourceHolder.isJarEntity())
		{
			JarFile jarFile = resourceHolder.getJarFile();
			JarEntry jarEntry = resourceHolder.getJarEntry();

			InputStream in = null;

			try
			{
				in = jarFile.getInputStream(jarEntry);
				return IOUtil.getBytes(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}
		else
		{
			InputStream in = null;

			try
			{
				in = IOUtil.getInputStream(resourceHolder.getFile());
				return IOUtil.getBytes(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}
	}

	/**
	 * 查找资源的{@linkplain ResourceHolder}。
	 * <p>
	 * 如果找不到资源，将返回{@code null}。
	 * </p>
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected ResourceHolder findResourceHolder(String resourcePath)
	{
		while (resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);

		// 尝试从文件路径加载
		if (this.path.isDirectory())
		{
			File file = new File(this.path, resourcePath);

			if (file.exists())
				return new ResourceHolder(resourcePath, file);
		}

		ResourceHolder resourceHolder = null;

		List<JarFileHolder> jarFileHolders = getJarFileHolders();

		for (JarFileHolder jarFileHolder : jarFileHolders)
		{
			JarFile jarFile = jarFileHolder.getJarFile();
			JarEntry jarEntry = jarFile.getJarEntry(resourcePath);

			if (jarEntry != null && resourceHolder == null)
				resourceHolder = new ResourceHolder(resourcePath, jarFileHolder, jarEntry);
			else
				IOUtil.close(jarFileHolder);
		}

		return resourceHolder;
	}

	/**
	 * 查找资源的{@linkplain ResourceHolder}列表。
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected List<ResourceHolder> findResourceHolders(String resourcePath)
	{
		while (resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);

		List<ResourceHolder> resourceInfos = new ArrayList<ResourceHolder>();

		// 尝试从文件路径加载
		if (this.path.isDirectory())
		{
			File file = new File(this.path, resourcePath);

			if (file.exists())
				resourceInfos.add(new ResourceHolder(resourcePath, file));

			return resourceInfos;
		}

		List<JarFileHolder> jarFileHolders = getJarFileHolders();

		for (JarFileHolder jarFileHolder : jarFileHolders)
		{
			JarFile jarFile = jarFileHolder.getJarFile();
			JarEntry jarEntry = jarFile.getJarEntry(resourcePath);

			if (jarEntry != null)
				resourceInfos.add(new ResourceHolder(resourcePath, jarFileHolder, jarEntry));
			else
				IOUtil.close(jarFileHolder);
		}

		return resourceInfos;
	}

	/**
	 * 获取{@linkplain #path}对应的所有{@linkplain JarFileHolder}列表。
	 * 
	 * @return
	 */
	protected List<JarFileHolder> getJarFileHolders()
	{
		if (this.holdJarFile)
			return this.jarFileHolders;
		else
		{
			List<JarFileHolder> jarFileHolders = new ArrayList<JarFileHolder>();
			findJarFileHolders(this.path, jarFileHolders, true);

			return jarFileHolders;
		}
	}

	/**
	 * 查找{@linkplain JarFile}，并将对应的{@linkplain JarFileHolder}写入指定列表。
	 * 
	 * @param path
	 * @param jarFileHolders
	 * @param closeJarFileOnClose
	 */
	protected void findJarFileHolders(File path, List<JarFileHolder> jarFileHolders, boolean closeJarFileOnClose)
	{
		List<File> files = new ArrayList<File>();

		if (path.isDirectory())
		{
			File[] listFiles = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return isJarFile(pathname);
				}
			});

			Collections.addAll(files, listFiles);
		}
		else
		{
			if (isJarFile(path))
				files.add(path);
		}

		for (File file : files)
		{
			try
			{
				JarFile jarFile = new JarFile(file);
				jarFileHolders.add(new JarFileHolder(jarFile, file, closeJarFileOnClose));
			}
			catch (IOException e)
			{
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("build " + JarFile.class.getSimpleName() + " for file [" + file
							+ "] error, it will be ignored", e);
			}
		}
	}

	/**
	 * 判断文件是否是jar文件。
	 * 
	 * @param file
	 * @return
	 */
	protected boolean isJarFile(File file)
	{
		if (file.isDirectory())
			return false;

		String fileName = file.getName().toLowerCase();

		return (fileName.endsWith(".jar") || fileName.endsWith(".zip"));
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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [path=" + path + "]";
	}

	protected static class ResourceHolder implements Closeable
	{
		/** 资源路径 */
		private String path;

		/** 资源对应的文件 */
		private File file;

		/** 当资源在jar文件内时，文件对应的JarFile */
		private JarFileHolder jarFileHolder;

		/** 当资源在jar文件内时，资源对应的JarEntity */
		private JarEntry jarEntry;

		public ResourceHolder(String path, File file)
		{
			super();
			this.path = path;
			this.file = file;
		}

		public ResourceHolder(String path, JarFileHolder jarFileHolder, JarEntry jarEntry)
		{
			super();
			this.path = path;
			this.file = jarFileHolder.getFile();
			this.jarFileHolder = jarFileHolder;
			this.jarEntry = jarEntry;
		}

		/**
		 * 获取资源路径。
		 * 
		 * @return
		 */
		public String getPath()
		{
			return path;
		}

		/**
		 * 获取资源对应的文件。
		 * 
		 * @return
		 */
		public File getFile()
		{
			return file;
		}

		/**
		 * 资源是否是jar内的资源。
		 * 
		 * @return
		 */
		public boolean isJarEntity()
		{
			return (this.jarEntry != null);
		}

		/**
		 * 当资源是jar内资源时，获取所在的jar文件。
		 * 
		 * @return
		 */
		public JarFile getJarFile()
		{
			return this.jarFileHolder.getJarFile();
		}

		/**
		 * 当资源是jar内资源时，获取资源对应的{@linkplain JarEntry}。
		 * 
		 * @return
		 */
		public JarEntry getJarEntry()
		{
			return jarEntry;
		}

		@Override
		public void close() throws IOException
		{
			if (this.jarFileHolder != null)
			{
				this.jarFileHolder.close();
			}
		}
	}

	/**
	 * {@linkplain JarFile}信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JarFileHolder implements Closeable
	{
		private JarFile jarFile;

		/** JarFile对应的文件 */
		private File file;

		private boolean closeJarFileOnClose;

		public JarFileHolder()
		{
			super();
		}

		public JarFileHolder(JarFile jarFile, File file, boolean closeJarFileOnClose)
		{
			super();
			this.jarFile = jarFile;
			this.file = file;
			this.closeJarFileOnClose = closeJarFileOnClose;
		}

		public JarFile getJarFile()
		{
			return jarFile;
		}

		public File getFile()
		{
			return file;
		}

		public boolean isCloseJarFileOnClose()
		{
			return closeJarFileOnClose;
		}

		@Override
		public void close() throws IOException
		{
			if (this.closeJarFileOnClose)
				this.jarFile.close();
			else
				;
		}
	}
}
