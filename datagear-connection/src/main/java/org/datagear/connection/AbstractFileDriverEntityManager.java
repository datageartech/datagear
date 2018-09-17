/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于文件的{@linkplain DriverEntityManager}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractFileDriverEntityManager implements DriverEntityManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileDriverEntityManager.class);

	public static final String DEFAULT_DRIVER_ENTITY_FILE_ENCODING = "UTF-8";

	private File rootDirectory;

	private String driverEntityInfoFileName;

	private String driverEntityFileEncoding = DEFAULT_DRIVER_ENTITY_FILE_ENCODING;

	private transient Map<String, PathDriverFactoryInfo> pathDriverFactoryInfoMap = new HashMap<String, PathDriverFactoryInfo>();

	private transient List<DriverEntity> driverEntities = null;

	private transient File driverEntityInfoFile = null;

	private transient long driverEntityInfoFileLastModified = -1;

	public AbstractFileDriverEntityManager()
	{
		super();
	}

	public AbstractFileDriverEntityManager(String rootDirectory, String driverEntityInfoFileName)
	{
		this(new File(rootDirectory), driverEntityInfoFileName);
	}

	public AbstractFileDriverEntityManager(File rootDirectory, String driverEntityInfoFileName)
	{
		super();
		this.rootDirectory = rootDirectory;
		this.driverEntityInfoFileName = driverEntityInfoFileName;
	}

	public File getRootDirectory()
	{
		return rootDirectory;
	}

	public void setRootDirectory(File rootDirectory)
	{
		this.rootDirectory = rootDirectory;
	}

	public String getDriverEntityInfoFileName()
	{
		return driverEntityInfoFileName;
	}

	public void setDriverEntityInfoFileName(String driverEntityInfoFileName)
	{
		this.driverEntityInfoFileName = driverEntityInfoFileName;
	}

	public String getDriverEntityFileEncoding()
	{
		return driverEntityFileEncoding;
	}

	public void setDriverEntityFileEncoding(String driverEntityFileEncoding)
	{
		this.driverEntityFileEncoding = driverEntityFileEncoding;
	}

	public File getDriverEntityInfoFile()
	{
		return new File(this.rootDirectory, this.driverEntityInfoFileName);
	}

	/**
	 * 初始化。
	 * 
	 * @throws DriverEntityManagerException
	 */
	public void init() throws DriverEntityManagerException
	{
		if (!this.rootDirectory.exists())
			this.rootDirectory.mkdirs();

		this.driverEntityInfoFile = getDriverEntityInfoFile();
		read();
	}

	@Override
	public synchronized void add(DriverEntity... driverEntities) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		for (DriverEntity driverEntity : driverEntities)
			checkInput(driverEntity);

		for (DriverEntity driverEntity : driverEntities)
		{
			removeExists(this.driverEntities, driverEntity.getId());

			this.driverEntities.add(driverEntity);
		}

		write();
	}

	@Override
	public boolean[] update(DriverEntity... driverEntities) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		for (DriverEntity driverEntity : driverEntities)
			checkInput(driverEntity);

		boolean[] updated = new boolean[driverEntities.length];

		for (int i = 0; i < driverEntities.length; i++)
		{
			DriverEntity driverEntity = driverEntities[i];

			int index = findDriverEntityIndex(this.driverEntities, driverEntity.getId());

			if (index >= 0)
			{
				this.driverEntities.set(index, driverEntity);
				updated[i] = true;
			}
			else
				updated[i] = false;
		}

		write();

		return updated;
	}

	@Override
	public synchronized DriverEntity get(String path) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		int index = findDriverEntityIndex(this.driverEntities, path);

		return (index < 0 ? null : this.driverEntities.get(index));
	}

	@Override
	public synchronized void delete(String... paths) throws DriverEntityManagerException
	{
		int removeCount = 0;

		for (int i = 0; i < paths.length; i++)
		{
			removeCount += removeExists(driverEntities, paths[i]);

			deleteDriverLibraryDirectory(paths[i]);
		}

		if (removeCount > 0)
			write();
	}

	@Override
	public synchronized List<DriverEntity> getAll() throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		return new ArrayList<DriverEntity>(this.driverEntities);
	}

	@Override
	public long getLastModified() throws DriverEntityManagerException
	{
		return this.driverEntityInfoFileLastModified;
	}

	@Override
	public synchronized void addDriverLibrary(DriverEntity driverEntity, String libraryName, InputStream in)
			throws DriverEntityManagerException
	{
		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		BufferedOutputStream out = null;

		try
		{
			out = new BufferedOutputStream(new FileOutputStream(file));
			write(in, out);
		}
		catch (FileNotFoundException e)
		{
			throw new DriverEntityManagerException(e);
		}
		finally
		{
			close(out);
		}
	}

	@Override
	public synchronized void deleteDriverLibrary(DriverEntity driverEntity, String... libraryName)
			throws DriverEntityManagerException
	{
		File directory = getDriverLibraryDirectory(driverEntity.getId(), false);

		if (!directory.exists())
			return;

		for (String ln : libraryName)
		{
			File file = new File(directory, ln);
			deleteFile(file);
		}
	}

	@Override
	public void deleteDriverLibrary(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		File directory = getDriverLibraryDirectory(driverEntity.getId(), false);

		deleteFileIn(directory);
	}

	@Override
	public InputStream getDriverLibrary(DriverEntity driverEntity, String libraryName)
			throws DriverEntityManagerException
	{
		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		try
		{
			return new BufferedInputStream(new FileInputStream(file));
		}
		catch (FileNotFoundException e)
		{
			throw new DriverLibraryNotFoundException(driverEntity, libraryName);
		}
	}

	@Override
	public void readDriverLibrary(DriverEntity driverEntity, String libraryName, OutputStream out)
			throws DriverEntityManagerException
	{
		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		InputStream in = null;

		try
		{
			in = new BufferedInputStream(new FileInputStream(file));
			write(in, out);
		}
		catch (FileNotFoundException e)
		{
			throw new DriverLibraryNotFoundException(driverEntity, libraryName);
		}
		finally
		{
			close(in);
		}
	}

	@Override
	public List<DriverLibraryInfo> getDriverLibraryInfos(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		List<DriverLibraryInfo> driverLibraryInfos = new ArrayList<DriverLibraryInfo>();

		File directory = getDriverLibraryDirectory(driverEntity.getId(), false);

		if (directory.exists())
		{
			File[] files = directory.listFiles();

			for (File file : files)
			{
				if (file.isDirectory())
					continue;

				DriverLibraryInfo driverLibraryInfo = new DriverLibraryInfo(file.getName(), file.length());

				driverLibraryInfos.add(driverLibraryInfo);
			}

		}

		return driverLibraryInfos;
	}

	@Override
	public Driver getDriver(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		PathDriverFactory pathDriverFactory = getPathDriverFactoryNotNull(driverEntity);

		return pathDriverFactory.getDriver(driverEntity.getDriverClassName());
	}

	@Override
	public void releaseDriver(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		removePathDriverFactory(driverEntity);
	}

	@Override
	public synchronized void releaseAllDrivers()
	{
		for (Map.Entry<String, PathDriverFactoryInfo> entry : this.pathDriverFactoryInfoMap.entrySet())
		{
			try
			{
				entry.getValue().getPathDriverFactory().release();
			}
			catch (Throwable t)
			{
				if (LOGGER.isErrorEnabled())
					LOGGER.error("releaseAllDrivers", t);
			}
		}

		this.pathDriverFactoryInfoMap = new HashMap<String, PathDriverFactoryInfo>();
	}

	/**
	 * 获取指定{@linkplain DriverEntity}的{@linkplain PathDriverFactory}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param driverEntity
	 * @return
	 * @throws PathDriverFactoryException
	 */
	protected synchronized PathDriverFactory getPathDriverFactoryNotNull(DriverEntity driverEntity)
			throws PathDriverFactoryException
	{
		String driverEntityId = driverEntity.getId();

		PathDriverFactory pathDriverFactory = null;

		PathDriverFactoryInfo pathDriverFactoryInfo = this.pathDriverFactoryInfoMap.get(driverEntityId);
		if (pathDriverFactoryInfo != null)
		{
			if (pathDriverFactoryInfo.isModifiedAfterCreation())
			{
				this.pathDriverFactoryInfoMap.remove(driverEntityId);
				pathDriverFactoryInfo.getPathDriverFactory().release();

				if (LOGGER.isDebugEnabled())
					LOGGER.debug(" [" + pathDriverFactory + "] has been discarded for its path modification");
			}
			else
				pathDriverFactory = pathDriverFactoryInfo.getPathDriverFactory();
		}

		if (pathDriverFactory == null)
		{
			pathDriverFactory = createPathDriverFactory(driverEntity);
			pathDriverFactoryInfo = new PathDriverFactoryInfo(pathDriverFactory);

			this.pathDriverFactoryInfoMap.put(driverEntityId, pathDriverFactoryInfo);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(" [" + pathDriverFactory + "] is created for loading drivers.");
		}

		return pathDriverFactory;
	}

	/**
	 * 移除{@linkplain PathDriverFactory}。
	 * 
	 * @param driverEntity
	 * @throws PathDriverFactoryException
	 */
	protected synchronized void removePathDriverFactory(DriverEntity driverEntity) throws PathDriverFactoryException
	{
		PathDriverFactoryInfo pathDriverFactoryInfo = this.pathDriverFactoryInfoMap.remove(driverEntity.getId());

		if (pathDriverFactoryInfo == null)
			return;

		pathDriverFactoryInfo.getPathDriverFactory().release();
	}

	/**
	 * 创建{@linkplain PathDriverFactory}实例并对其进行初始化。
	 * 
	 * @param driverEntity
	 * @return
	 * @throws PathDriverFactoryException
	 */
	protected PathDriverFactory createPathDriverFactory(DriverEntity driverEntity) throws PathDriverFactoryException
	{
		File path = getDriverLibraryDirectory(driverEntity.getId(), true);

		PathDriverFactory pathDriverFactory = new PathDriverFactory(path);
		pathDriverFactory.init();

		return pathDriverFactory;
	}

	/**
	 * 校验输入。
	 * 
	 * @param driverEntity
	 */
	protected void checkInput(DriverEntity driverEntity)
	{
		if (isBlank(driverEntity.getId()) || isBlank(driverEntity.getDriverClassName()))
			throw new IllegalArgumentException();
	}

	protected boolean reloadDriverEntityFileIfModified()
	{
		long thisModified = this.driverEntityInfoFile.lastModified();

		if (thisModified == this.driverEntityInfoFileLastModified)
			return false;

		read();

		return true;
	}

	/**
	 * 从列表中移除相同的{@linkplain DriverEntity}。
	 * 
	 * @param driverEntities
	 * @param driverEntityId
	 * @return
	 */
	protected int removeExists(List<DriverEntity> driverEntities, String driverEntityId)
	{
		int removeCount = 0;

		int index = -1;
		while ((index = findDriverEntityIndex(driverEntities, driverEntityId)) >= 0)
		{
			driverEntities.remove(index);
			removeCount++;
		}

		return removeCount;
	}

	/**
	 * 查找指定路径的{@linkplain DriverEntity}索引位置。
	 * 
	 * @param driverEntities
	 * @param driverEntityId
	 * @return
	 */
	protected int findDriverEntityIndex(List<DriverEntity> driverEntities, String driverEntityId)
	{
		if (driverEntities != null)
		{
			for (int i = driverEntities.size() - 1; i >= 0; i--)
			{
				DriverEntity driverEntity = driverEntities.get(i);

				if (driverEntity.getId().equals(driverEntityId))
					return i;
			}
		}

		return -1;
	}

	/**
	 * 读取{@linkplain #driverEntities}。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected void read() throws DriverEntityManagerException
	{
		List<DriverEntity> driverEntities = null;

		if (this.driverEntityInfoFile.exists())
			driverEntities = readDriverEntities(this.driverEntityInfoFile);
		else
			driverEntities = new ArrayList<DriverEntity>();

		this.driverEntities = driverEntities;
		this.driverEntityInfoFileLastModified = this.driverEntityInfoFile.lastModified();
	}

	/**
	 * 从文件中读取{@linkplain DriverEntity}列表。
	 * 
	 * @param driverEntityInfoFile
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected abstract List<DriverEntity> readDriverEntities(File driverEntityInfoFile)
			throws DriverEntityManagerException;

	/**
	 * 将{@linkplain #driverEntities}列表写入文件。
	 * 
	 * @throws DriverEntityManagerException
	 */
	protected void write() throws DriverEntityManagerException
	{
		writeDriverEntities(this.driverEntities, this.driverEntityInfoFile);
	}

	/**
	 * 将{@linkplain DriverEntity}列表写入文件。
	 * 
	 * @param driverEntities
	 * @param driverEntityInfoFile
	 * @throws DriverEntityManagerException
	 */
	protected abstract void writeDriverEntities(List<DriverEntity> driverEntities, File driverEntityInfoFile)
			throws DriverEntityManagerException;

	/**
	 * 获取{@linkplain #driverEntityInfoFile}输入流。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected Reader getDriverEntityInfoFileReader() throws DriverEntityManagerException
	{
		try
		{
			return new BufferedReader(new InputStreamReader(new FileInputStream(this.driverEntityInfoFile),
					this.driverEntityFileEncoding));
		}
		catch (FileNotFoundException e)
		{
			throw new DriverEntityManagerException(e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	/**
	 * 获取{@linkplain #driverEntityInfoFile}输出流。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected Writer getDriverEntityInfoFileWriter() throws DriverEntityManagerException
	{
		try
		{
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.driverEntityInfoFile),
					this.driverEntityFileEncoding));
		}
		catch (FileNotFoundException e)
		{
			throw new DriverEntityManagerException(e);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	/**
	 * 字符串是否为空。
	 * 
	 * @param s
	 * @return
	 */
	protected boolean isBlank(String s)
	{
		if (s == null)
			return true;

		if (s.isEmpty())
			return true;

		if (s.trim().isEmpty())
			return true;

		return false;
	}

	protected void close(Closeable closeable)
	{
		if (closeable == null)
			return;

		try
		{
			closeable.close();
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * 获取驱动库文件。
	 * 
	 * @param driverEntityId
	 * @param driverLibraryFileName
	 * @return
	 */
	protected File getDriverLibraryFile(String driverEntityId, String driverLibraryFileName)
	{
		File path = getDriverLibraryDirectory(driverEntityId, true);
		File file = new File(path, driverLibraryFileName);

		return file;
	}

	/**
	 * 获取驱动库目录。
	 * 
	 * @param driverEntityId
	 * @param create
	 * @return
	 */
	protected File getDriverLibraryDirectory(String driverEntityId, boolean create)
	{
		File file = new File(this.rootDirectory, driverEntityId);

		if (create && !file.exists())
			file.mkdirs();

		return file;
	}

	/**
	 * 删除目录下指定名称的文件。
	 * 
	 * @param driverEntityId
	 */
	protected void deleteDriverLibraryDirectory(String driverEntityId)
	{
		File file = getDriverLibraryDirectory(driverEntityId, false);
		deleteFile(file);
	}

	/**
	 * 删除文件。
	 * 
	 * @param file
	 */
	protected void deleteFile(File file)
	{
		if (!file.exists())
			return;

		if (file.isDirectory())
		{
			File[] children = file.listFiles();

			for (File child : children)
				deleteFile(child);
		}

		file.delete();
	}

	/**
	 * 删除文件夹里的所有文件。
	 * 
	 * @param directory
	 */
	protected void deleteFileIn(File directory)
	{
		if (!directory.exists())
			return;

		if (directory.isDirectory())
		{
			File[] children = directory.listFiles();

			for (File child : children)
				deleteFile(child);
		}
	}

	/**
	 * 读取输入流，并写入输出流。
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	protected void write(InputStream in, OutputStream out) throws DriverEntityManagerException
	{
		byte[] cache = new byte[1024];
		int readLen = -1;

		try
		{
			while ((readLen = in.read(cache)) > -1)
				out.write(cache, 0, readLen);
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	protected static class PathDriverFactoryInfo
	{
		private final PathDriverFactory pathDriverFactory;

		private final long lastModifiedOnCreation;

		public PathDriverFactoryInfo(PathDriverFactory pathDriverFactory)
		{
			super();

			this.pathDriverFactory = pathDriverFactory;
			this.lastModifiedOnCreation = this.pathDriverFactory.getPathLastModified();
		}

		public PathDriverFactory getPathDriverFactory()
		{
			return pathDriverFactory;
		}

		public long getLastModifiedOnCreation()
		{
			return lastModifiedOnCreation;
		}

		public boolean isModifiedAfterCreation()
		{
			return this.pathDriverFactory.getPathLastModified() > this.lastModifiedOnCreation;
		}
	}
}
