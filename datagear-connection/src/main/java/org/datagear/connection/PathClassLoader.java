/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class PathClassLoader extends ClassLoader
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PathClassLoader.class);

	private static final String CLASS_FILE_SUFFIX = ".class";

	/** 类路径 */
	private File path;

	/** 要强制加载类路径之外的类名套集 */
	private Set<String> outsideForceLoads;

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

	public void setOutsideForceLoads(String... outsideForceLoads)
	{
		this.outsideForceLoads = new HashSet<String>();

		for (String outsideForceLoad : outsideForceLoads)
			this.outsideForceLoads.add(outsideForceLoad);
	}

	/**
	 * 关闭。
	 */
	public void close()
	{
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		ResourceInfo resourceInfo = findResourceInfo(name);

		if (resourceInfo == null)
		{
			ClassLoader parent = getParent();
			if (parent == null)
				parent = PathClassLoader.class.getClassLoader();

			return parent.getResourceAsStream(name);
		}

		byte[] bytes = null;

		try
		{
			bytes = getResourceBytes(resourceInfo);
		}
		catch (IOException e)
		{
			return null;
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Got resource as stream " + "[" + name + "] in file ["
					+ resourceInfo.getFileOfResource().getPath() + "]");

		return new ByteArrayInputStream(bytes);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		Class<?> clazz = findLoadedClass(name);

		if (clazz == null)
		{
			try
			{
				clazz = findClass(name);
			}
			catch (Exception e)
			{
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

				clazz = defineClass(name, bytes, 0, bytes.length);
			}
		}

		if (clazz == null)
		{
			ClassLoader parent = getParent();
			if (parent == null)
				parent = PathClassLoader.class.getClassLoader();

			// 先尝试父加载器加载类，这样可以避免Java标准库被覆盖
			if (clazz == null)
			{
				try
				{
					clazz = Class.forName(name, false, parent);
				}
				catch (ClassNotFoundException e)
				{
				}
			}
		}

		if (resolve)
			resolveClass(clazz);

		return clazz;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		String classFilePath = classNameToPath(name);

		ResourceInfo classResourceInfo = findResourceInfo(classFilePath);

		if (classResourceInfo == null)
			throw new ClassNotFoundException(name);

		byte[] classBytes = null;

		try
		{
			classBytes = getResourceBytes(classResourceInfo);
		}
		catch (IOException e)
		{
			throw new ClassNotFoundException(name, e);
		}

		Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Found and loaded class [" + name + "] in file ["
					+ classResourceInfo.getFileOfResource().getPath() + "]");

		return clazz;
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
		try
		{
			return getBytes(resourceInfo.getIn());
		}
		finally
		{
			close(resourceInfo.getIn());

			if (resourceInfo.hasJarFileOfResource())
				close(resourceInfo.getJarFileOfResource());
		}

	}

	/**
	 * 查找资源的{@linkplain ResourceInfo}。
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected ResourceInfo findResourceInfo(String resourcePath)
	{
		InputStream in = null;
		File fileOfResource = null;
		JarFile jarFileOfResource = null;

		// 尝试从文件路径加载
		if (this.path.isDirectory())
		{
			File file = new File(this.path, resourcePath);

			if (file.exists())
			{
				try
				{
					in = new FileInputStream(file);
					fileOfResource = file;
				}
				catch (FileNotFoundException e)
				{
					in = null;
				}
			}
		}

		if (in == null)
		{
			List<File> files = getFilesOfJar(this.path);

			for (int i = 0, len = files.size(); i < len; i++)
			{
				File file = files.get(i);

				boolean isMe = false;
				JarFile tmpJarFile = null;

				try
				{
					tmpJarFile = new JarFile(file);
					JarEntry jarEntry = tmpJarFile.getJarEntry(resourcePath);

					if (jarEntry != null)
					{
						in = tmpJarFile.getInputStream(jarEntry);
						fileOfResource = file;
						jarFileOfResource = tmpJarFile;
						isMe = true;
						break;
					}
				}
				catch (IOException e)
				{
				}
				finally
				{
					if (!isMe)
						close(tmpJarFile);
				}
			}
		}

		if (in == null)
			return null;

		ResourceInfo resourceInfo = new ResourceInfo(in, fileOfResource, jarFileOfResource);

		return resourceInfo;
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
	 * 获取jar文件列表。
	 * 
	 * @param path
	 * @return
	 */
	protected List<File> getFilesOfJar(File path)
	{
		if (path.isDirectory())
		{
			File[] files = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return isJarFile(pathname);
				}
			});

			return Arrays.asList(files);
		}
		else
		{
			List<File> jarFiles = new ArrayList<File>();

			if (isJarFile(path))
				jarFiles.add(path);

			return jarFiles;
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
		private InputStream in;

		private File fileOfResource;

		private JarFile jarFileOfResource;

		public ResourceInfo()
		{
			super();
		}

		public ResourceInfo(InputStream in, File fileOfResource, JarFile jarFileOfResource)
		{
			super();
			this.in = in;
			this.fileOfResource = fileOfResource;
			this.jarFileOfResource = jarFileOfResource;
		}

		public InputStream getIn()
		{
			return in;
		}

		public void setIn(InputStream in)
		{
			this.in = in;
		}

		public File getFileOfResource()
		{
			return fileOfResource;
		}

		public void setFileOfResource(File fileOfResource)
		{
			this.fileOfResource = fileOfResource;
		}

		public boolean hasJarFileOfResource()
		{
			return jarFileOfResource != null;
		}

		public JarFile getJarFileOfResource()
		{
			return jarFileOfResource;
		}

		public void setJarFileOfResource(JarFile jarFileOfResource)
		{
			this.jarFileOfResource = jarFileOfResource;
		}
	}
}
