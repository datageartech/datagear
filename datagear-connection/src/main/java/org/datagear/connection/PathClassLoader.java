/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 特定路径类加载器。
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

	/** 此目录中的jar文件列表 */
	private transient List<JarFileInfo> jarFileInfos = new ArrayList<JarFileInfo>();

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

		findJarFileInfos(this.path, this.jarFileInfos);
	}

	/**
	 * 获取路径。
	 * 
	 * @return
	 */
	public File getPath()
	{
		return this.path;
	}

	/**
	 * 获取路径对应的所有{@linkplain JarFileInfo}列表。
	 * 
	 * @return
	 */
	public List<JarFileInfo> getJarFileInfos()
	{
		return this.jarFileInfos;
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
	 * 关闭。
	 * <p>
	 * 关闭将释放占用资源，并使之不再可用。
	 * </p>
	 */
	@Override
	public void close()
	{
		for (JarFileInfo jarFileInfo : this.jarFileInfos)
		{
			JarFile jarFile = jarFileInfo.getJarFile();
			close(jarFile);
		}
	}

	@Override
	public URL getResource(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URL for [" + name + "]");

		ResourceInfo resourceInfo = findResourceInfo(name);

		if (resourceInfo == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URL for [" + name + "]");

			return parent.getResource(name);
		}

		try
		{
			URL url = getResourceURL(resourceInfo);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("got resource URL [" + url + "] for [" + name + "] in file ["
						+ resourceInfo.getFile().getPath() + "]");

			return url;
		}
		catch (IOException e)
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("getting resource URL for [" + name + "] in file [" + resourceInfo.getFile().getPath()
						+ "] error", e);
		}

		return null;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource URLs for [" + name + "]");

		List<ResourceInfo> resourceInfos = findResourceInfos(name);

		if (resourceInfos == null || resourceInfos.isEmpty())
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource URLs for [" + name + "]");

			return parent.getResources(name);
		}

		try
		{
			Vector<URL> urls = new Vector<URL>();

			for (ResourceInfo resourceInfo : resourceInfos)
			{
				URL url = getResourceURL(resourceInfo);
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

		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start getting resource as stream for [" + name + "]");

		ResourceInfo resourceInfo = findResourceInfo(name);

		if (resourceInfo == null)
		{
			ClassLoader parent = getParentClassLoader();

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("delegate parent class loader for getting resource as stream for [" + name + "]");

			return parent.getResourceAsStream(name);
		}

		try
		{
			byte[] bytes = getResourceBytes(resourceInfo);

			InputStream in = new ByteArrayInputStream(bytes);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(
						"got resource as stream for [" + name + "] in file [" + resourceInfo.getFile().getPath() + "]");

			return in;
		}
		catch (IOException e)
		{
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("getting resource as stream for [" + name + "] in file ["
						+ resourceInfo.getFile().getPath() + "] error", e);
		}

		return null;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
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
				}
				catch (Throwable t)
				{
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("finding class [" + name + "] error", t);
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
					bytes = getBytes(in);
				}
				catch (IOException e)
				{
					throw new ClassNotFoundException(name, e);
				}
				finally
				{
					close(in);
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
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("start finding class [" + name + "]");

		String classFilePath = classNameToPath(name);

		ResourceInfo classResourceInfo = findResourceInfo(classFilePath);

		if (classResourceInfo == null)
			throw new ClassNotFoundException(name);

		byte[] classBytes = null;

		try
		{
			classBytes = getResourceBytes(classResourceInfo);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("found class for [" + name + "] in file [" + classResourceInfo.getFile().getPath() + "]");
		}
		catch (IOException e)
		{
			throw new ClassNotFoundException(name, e);
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
	 * 获取{@linkplain ResourceInfo}的{@linkplain URL}。
	 * 
	 * @param resourceInfo
	 * @return
	 * @throws IOException
	 */
	protected URL getResourceURL(ResourceInfo resourceInfo) throws IOException
	{
		if (resourceInfo.isJarEntity())
		{
			StringBuilder sb = new StringBuilder("jar:");
			sb.append(resourceInfo.getFile().toURI().toURL().toString());
			sb.append("!/");
			sb.append(resourceInfo.getJarEntry().getName());

			return new URL(sb.toString());
		}
		else
			return resourceInfo.getFile().toURI().toURL();
	}

	/**
	 * 获取{@linkplain ResourceInfo}的字节数组。
	 * 
	 * @param resourceInfo
	 * @return
	 * @throws IOException
	 */
	protected byte[] getResourceBytes(ResourceInfo resourceInfo) throws IOException
	{
		if (resourceInfo.isJarEntity())
		{
			JarFile jarFile = resourceInfo.getJarFile();
			JarEntry jarEntry = resourceInfo.getJarEntry();

			InputStream in = null;

			try
			{
				in = jarFile.getInputStream(jarEntry);
				return getBytes(in);
			}
			finally
			{
				close(in);
			}
		}
		else
		{
			BufferedInputStream in = null;

			try
			{
				in = new BufferedInputStream(new FileInputStream(resourceInfo.getFile()));
				return getBytes(in);
			}
			finally
			{
				close(in);
			}
		}
	}

	/**
	 * 查找资源的{@linkplain ResourceInfo}。
	 * <p>
	 * 如果找不到资源，将返回{@code null}。
	 * </p>
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected ResourceInfo findResourceInfo(String resourcePath)
	{
		while (resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);

		// 尝试从文件路径加载
		if (this.path.isDirectory())
		{
			File file = new File(this.path, resourcePath);

			if (file.exists())
				return new ResourceInfo(resourcePath, file);
		}

		for (JarFileInfo jarFileInfo : this.jarFileInfos)
		{
			JarFile jarFile = jarFileInfo.getJarFile();
			JarEntry jarEntry = jarFile.getJarEntry(resourcePath);

			if (jarEntry != null)
				return new ResourceInfo(resourcePath, jarFileInfo.getFile(), jarFile, jarEntry);
		}

		return null;
	}

	/**
	 * 查找资源的{@linkplain ResourceInfo}列表。
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected List<ResourceInfo> findResourceInfos(String resourcePath)
	{
		while (resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);

		List<ResourceInfo> resourceInfos = new ArrayList<ResourceInfo>();

		// 尝试从文件路径加载
		if (this.path.isDirectory())
		{
			File file = new File(this.path, resourcePath);

			if (file.exists())
				resourceInfos.add(new ResourceInfo(resourcePath, file));

			return resourceInfos;
		}

		for (JarFileInfo jarFileInfo : this.jarFileInfos)
		{
			JarFile jarFile = jarFileInfo.getJarFile();
			JarEntry jarEntry = jarFile.getJarEntry(resourcePath);

			if (jarEntry != null)
				resourceInfos.add(new ResourceInfo(resourcePath, jarFileInfo.getFile(), jarFile, jarEntry));
		}

		return resourceInfos;
	}

	/**
	 * 读取输入流字节数组。
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected byte[] getBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int readLen = 0;

		while ((readLen = in.read(buffer)) > 0)
			bos.write(buffer, 0, readLen);

		byte[] bytes = bos.toByteArray();

		return bytes;
	}

	/**
	 * 查找{@linkplain JarFile}，并将对应的{@linkplain JarFileInfo}写入指定列表。
	 * 
	 * @param path
	 * @param jarFileInfos
	 */
	protected void findJarFileInfos(File path, List<JarFileInfo> jarFileInfos)
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
				jarFileInfos.add(new JarFileInfo(jarFile, file));
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

	/**
	 * 关闭{@linkplain Closeable}。
	 */
	protected void close(Closeable closeable)
	{
		if (closeable == null)
			return;

		try
		{
			closeable.close();
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * 关闭{@linkplain ZipFile}。
	 * 
	 * @param zipFile
	 */
	protected void close(ZipFile zipFile)
	{
		if (zipFile == null)
			return;

		try
		{
			zipFile.close();
		}
		catch (Throwable t)
		{
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [path=" + path + "]";
	}

	protected static class ResourceInfo
	{
		/** 资源路径 */
		private String path;

		/** 资源对应的文件 */
		private File file;

		/** 当资源在jar文件内时，文件对应的JarFile */
		private JarFile jarFile;

		/** 当资源在jar文件内时，资源对应的JarEntity */
		private JarEntry jarEntry;

		public ResourceInfo(String path, File file)
		{
			super();
			this.path = path;
			this.file = file;
		}

		public ResourceInfo(String path, File file, JarFile jarFile, JarEntry jarEntry)
		{
			super();
			this.path = path;
			this.file = file;
			this.jarFile = jarFile;
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
			return (this.jarFile != null);
		}

		/**
		 * 当资源是jar内资源时，获取所在的jar文件。
		 * 
		 * @return
		 */
		public JarFile getJarFile()
		{
			return jarFile;
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
	}

	/**
	 * {@linkplain JarFile}信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class JarFileInfo
	{
		private JarFile jarFile;

		private File file;

		public JarFileInfo()
		{
			super();
		}

		public JarFileInfo(JarFile jarFile, File file)
		{
			super();
			this.jarFile = jarFile;
			this.file = file;
		}

		public JarFile getJarFile()
		{
			return jarFile;
		}

		public void setJarFile(JarFile jarFile)
		{
			this.jarFile = jarFile;
		}

		public File getFile()
		{
			return file;
		}

		public void setFile(File file)
		{
			this.file = file;
		}
	}
}
