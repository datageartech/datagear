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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于文件的{@linkplain DriverEntityManager}。
 * <p>
 * 此类实例在使用前需要调用其{@linkplain #init()}方法，在弃用前，需要调用其{@linkplain #releaseAll()}方法。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractFileDriverEntityManager implements DriverEntityManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileDriverEntityManager.class);

	public static final String DEFAULT_DRIVER_ENTITY_FILE_ENCODING = IOUtil.CHARSET_UTF_8;

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
		this(FileUtil.getDirectory(rootDirectory), driverEntityInfoFileName);
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
		return FileUtil.getFile(this.rootDirectory, this.driverEntityInfoFileName);
	}

	/**
	 * 初始化。
	 * 
	 * @throws DriverEntityManagerException
	 */
	public synchronized void init() throws DriverEntityManagerException
	{
		if (!this.rootDirectory.exists())
			this.rootDirectory.mkdirs();

		this.driverEntityInfoFile = getDriverEntityInfoFile();
		readDriverEntities();
	}

	@Override
	public synchronized void add(DriverEntity... driverEntities) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		for (DriverEntity driverEntity : driverEntities)
			checkValidDriverEntity(driverEntity);

		for (DriverEntity driverEntity : driverEntities)
		{
			removeExists(this.driverEntities, driverEntity.getId());
			this.driverEntities.add(driverEntity);
		}

		writeDriverEntities();
	}

	@Override
	public synchronized boolean[] update(DriverEntity... driverEntities) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		for (DriverEntity driverEntity : driverEntities)
			checkValidDriverEntity(driverEntity);

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

		writeDriverEntities();

		return updated;
	}

	@Override
	public synchronized DriverEntity get(String id) throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		int index = findDriverEntityIndex(this.driverEntities, id);

		return (index < 0 ? null : this.driverEntities.get(index));
	}

	@Override
	public synchronized void delete(String... ids) throws DriverEntityManagerException
	{
		int removeCount = 0;

		for (int i = 0; i < ids.length; i++)
		{
			removeCount += removeExists(driverEntities, ids[i]);

			// 需先释放资源
			removePathDriverFactoryInfo(ids[i]);
			deleteDriverLibraryDirectory(ids[i]);
		}

		if (removeCount > 0)
			writeDriverEntities();
	}

	@Override
	public synchronized List<DriverEntity> getAll() throws DriverEntityManagerException
	{
		reloadDriverEntityFileIfModified();

		return Collections.unmodifiableList(this.driverEntities);
	}

	@Override
	public long getLastModified() throws DriverEntityManagerException
	{
		return this.driverEntityInfoFileLastModified;
	}

	@Override
	public synchronized long getLastModified(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		PathDriverFactoryInfo pdfi = getPathDriverFactoryInfoNonNull(driverEntity, false);
		return pdfi.getLastModified();
	}

	@Override
	public synchronized void addDriverLibrary(DriverEntity driverEntity, String libraryName, InputStream in)
			throws DriverEntityManagerException
	{
		// 需先释放资源
		removePathDriverFactoryInfo(driverEntity);

		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		BufferedOutputStream out = null;

		try
		{
			out = new BufferedOutputStream(new FileOutputStream(file));
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	@Override
	public synchronized boolean[] deleteDriverLibrary(DriverEntity driverEntity, String... libraryName)
			throws DriverEntityManagerException
	{
		// 需先释放资源
		removePathDriverFactoryInfo(driverEntity);

		File directory = getDriverLibraryDirectory(driverEntity.getId(), false);

		boolean[] deleted = new boolean[libraryName.length];

		if (!directory.exists())
		{
			Arrays.fill(deleted, true);
			return deleted;
		}

		for (int i = 0; i < libraryName.length; i++)
		{
			String ln = libraryName[i];
			File file = FileUtil.getFile(directory, ln);

			deleted[i] = FileUtil.deleteFile(file);
		}

		return deleted;
	}

	@Override
	public synchronized boolean deleteDriverLibrary(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		// 需先释放资源
		removePathDriverFactoryInfo(driverEntity);

		File directory = getDriverLibraryDirectory(driverEntity.getId(), false);
		return FileUtil.clearDirectory(directory);
	}

	@Override
	public synchronized InputStream getDriverLibrary(DriverEntity driverEntity, String libraryName)
			throws DriverEntityManagerException
	{
		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		try
		{
			return IOUtil.getInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			throw new DriverLibraryNotFoundException(driverEntity, libraryName);
		}
	}

	@Override
	public synchronized void readDriverLibrary(DriverEntity driverEntity, String libraryName, OutputStream out)
			throws DriverEntityManagerException
	{
		File file = getDriverLibraryFile(driverEntity.getId(), libraryName);

		InputStream in = null;

		try
		{
			in = IOUtil.getInputStream(file);
			IOUtil.write(in, out);
		}
		catch (IOException e)
		{
			throw new DriverLibraryNotFoundException(driverEntity, libraryName);
		}
		finally
		{
			IOUtil.close(in);
		}
	}

	@Override
	public synchronized List<DriverLibraryInfo> getDriverLibraryInfos(DriverEntity driverEntity)
			throws DriverEntityManagerException
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
	public synchronized Driver getDriver(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		PathDriverFactoryInfo pdfi = getPathDriverFactoryInfoNonNull(driverEntity, true);
		return pdfi.getPathDriverFactory().getDriver(driverEntity.getDriverClassName());
	}

	@Override
	public synchronized void release(DriverEntity driverEntity) throws DriverEntityManagerException
	{
		removePathDriverFactoryInfo(driverEntity);
	}

	@Override
	public synchronized void releaseAll()
	{
		for (Map.Entry<String, PathDriverFactoryInfo> entry : this.pathDriverFactoryInfoMap.entrySet())
		{
			releasePathDriverFactory(entry.getValue());
		}

		this.pathDriverFactoryInfoMap = new HashMap<String, PathDriverFactoryInfo>();
	}

	@Override
	public synchronized void exportToZip(ZipOutputStream out, String... ids) throws DriverEntityManagerException
	{
		List<DriverEntity> exported = null;

		if (ids == null || ids.length == 0)
			exported = this.driverEntities;
		else
		{
			exported = new ArrayList<DriverEntity>();
			for (DriverEntity driverEntity : this.driverEntities)
			{
				if (isValidDriverEntityForIdArray(driverEntity, ids))
					exported.add(driverEntity);
			}
		}

		try
		{
			ZipEntry driverEntityInfoZipEntry = new ZipEntry(this.driverEntityInfoFileName);
			out.putNextEntry(driverEntityInfoZipEntry);

			Writer writer = getDriverEntityInfoFileWriter(out);

			try
			{
				writeDriverEntities(writer, exported);
			}
			finally
			{
				IOUtil.flush(writer);
			}
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}

		try
		{
			for (DriverEntity driverEntity : exported)
			{
				File libraryDirectory = getDriverLibraryDirectory(driverEntity.getId(), false);
				IOUtil.writeFileToZipOutputStream(out, libraryDirectory, libraryDirectory.getName());
			}
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	@Override
	public synchronized void importFromZip(ZipInputStream in, String... ids) throws DriverEntityManagerException
	{
		ZipEntry zipEntry = null;

		try
		{
			while ((zipEntry = in.getNextEntry()) != null)
			{
				if (isDriverEntityInfoFileZipEntry(zipEntry))
				{
					// 这里不能直接使用in，readDriverEntities实现会关闭in导致后面无法读取
					Reader reader = getDriverEntityInfoFileReader(IOUtil.getByteArrayInputStream(in));

					try
					{
						List<DriverEntity> driverEntities = readDriverEntities(reader);

						for (DriverEntity driverEntity : driverEntities)
						{
							if (isValidDriverEntityForIdArray(driverEntity, ids))
							{
								removeExists(this.driverEntities, driverEntity.getId());
								this.driverEntities.add(driverEntity);
							}
						}
					}
					finally
					{
						IOUtil.close(reader);
					}
				}
				else if (isValidZipEntryForIdArray(zipEntry, ids))
				{
					File file = getFileInRootDirectory(zipEntry.getName());

					if (zipEntry.isDirectory())
					{
						if (file.exists())
							FileUtil.clearDirectory(file);
						else
							file.mkdirs();
					}
					else
					{
						OutputStream out = null;

						try
						{
							out = new FileOutputStream(file);
							IOUtil.write(in, out);
						}
						finally
						{
							IOUtil.close(out);
						}
					}
				}

				in.closeEntry();
			}

			writeDriverEntities();
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	@Override
	public List<DriverEntity> readDriverEntitiesFromZip(ZipInputStream in) throws DriverEntityManagerException
	{
		List<DriverEntity> driverEntities = null;

		ZipEntry zipEntry = null;
		try
		{
			while ((zipEntry = in.getNextEntry()) != null)
			{
				if (isDriverEntityInfoFileZipEntry(zipEntry))
				{
					// 这里不能直接使用in，readDriverEntities实现会关闭in导致后面无法读取
					Reader reader = getDriverEntityInfoFileReader(IOUtil.getByteArrayInputStream(in));

					try
					{
						driverEntities = readDriverEntities(reader);
					}
					finally
					{
						IOUtil.close(reader);
					}
				}

				in.closeEntry();
			}

			return (driverEntities == null ? new ArrayList<DriverEntity>(0) : driverEntities);
		}
		catch (IOException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	/**
	 * 判断给定{@linkplain ZipEntry}是否是{@linkplain DriverEntity}信息文件。
	 * 
	 * @param zipEntry
	 * @return
	 */
	protected boolean isDriverEntityInfoFileZipEntry(ZipEntry zipEntry)
	{
		String name = zipEntry.getName();

		return name.equals(this.driverEntityInfoFileName);
	}

	/**
	 * 判断给定{@linkplain ZipEntry}是否是指定{@linkplain DriverEntity#getId()}数组元素的库。
	 * 
	 * @param zipEntry
	 * @param driverEntityIds
	 * @return
	 */
	protected boolean isValidZipEntryForIdArray(ZipEntry zipEntry, String... driverEntityIds)
	{
		if (driverEntityIds == null || driverEntityIds.length == 0)
			return true;

		String name = zipEntry.getName();

		for (String driverEntityId : driverEntityIds)
		{
			if (name.startsWith(driverEntityId))
				return true;
		}

		return false;
	}

	/**
	 * 是否在数组中有效。
	 * 
	 * @param driverEntity
	 * @param ids
	 * @return
	 */
	protected boolean isValidDriverEntityForIdArray(DriverEntity driverEntity, String... ids)
	{
		if (ids == null || ids.length == 0)
			return true;

		String myId = driverEntity.getId();

		for (String id : ids)
		{
			if (myId.equals(id))
				return true;
		}

		return false;
	}

	/**
	 * 获取指定{@linkplain DriverEntity}的{@linkplain PathDriverFactoryInfo}。
	 * <p>
	 * 此方法不会返回{@code null}。
	 * </p>
	 * 
	 * @param driverEntity
	 * @param renewIfModified
	 *            是否在有修改时重新创建
	 * @return
	 * @throws PathDriverFactoryException
	 */
	protected PathDriverFactoryInfo getPathDriverFactoryInfoNonNull(DriverEntity driverEntity, boolean renewIfModified)
			throws PathDriverFactoryException
	{
		String driverEntityId = driverEntity.getId();

		PathDriverFactoryInfo pdzfi = this.pathDriverFactoryInfoMap.get(driverEntityId);

		if (pdzfi != null && renewIfModified && pdzfi.isModifiedAfterCreation())
		{
			removePathDriverFactoryInfo(driverEntity);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(pdzfi + " is discarded for modification");

			pdzfi = null;
		}

		if (pdzfi == null)
		{
			pdzfi = new PathDriverFactoryInfo(createPathDriverFactory(driverEntity));

			this.pathDriverFactoryInfoMap.put(driverEntityId, pdzfi);

			if (LOGGER.isDebugEnabled())
				LOGGER.debug(pdzfi + " is created for loading drivers.");
		}

		return pdzfi;
	}

	/**
	 * 移除并释放{@linkplain PathDriverFactoryInfo}。
	 * 
	 * @param driverEntity
	 */
	protected void removePathDriverFactoryInfo(DriverEntity driverEntity)
	{
		removePathDriverFactoryInfo(driverEntity.getId());
	}

	/**
	 * 移除并释放{@linkplain PathDriverFactoryInfo}。
	 * 
	 * @param driverEntityId
	 */
	protected void removePathDriverFactoryInfo(String driverEntityId)
	{
		PathDriverFactoryInfo pathDriverFactoryInfo = this.pathDriverFactoryInfoMap.remove(driverEntityId);
		releasePathDriverFactory(pathDriverFactoryInfo);
	}

	protected void releasePathDriverFactory(PathDriverFactoryInfo factoryInfo)
	{
		if (factoryInfo == null)
			return;

		factoryInfo.getPathDriverFactory().release();
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
	 * 校验{@linkplain DriverEntity}。
	 * 
	 * @param driverEntity
	 * @throws IllegalArgumentException
	 */
	protected void checkValidDriverEntity(DriverEntity driverEntity) throws IllegalArgumentException
	{
		if (!isValidDriverEntity(driverEntity))
			throw new IllegalArgumentException();
	}

	/**
	 * 是否是合法的{@linkplain DriverEntity}。
	 * 
	 * @param driverEntity
	 * @return
	 */
	protected boolean isValidDriverEntity(DriverEntity driverEntity)
	{
		return (!isBlank(driverEntity.getId()) && !isBlank(driverEntity.getDriverClassName()));
	}

	protected boolean reloadDriverEntityFileIfModified()
	{
		long thisModified = this.driverEntityInfoFile.lastModified();

		if (thisModified == this.driverEntityInfoFileLastModified)
			return false;

		readDriverEntities();

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
	protected void readDriverEntities() throws DriverEntityManagerException
	{
		List<DriverEntity> driverEntities = null;

		if (this.driverEntityInfoFile.exists())
		{
			Reader in = getDriverEntityInfoFileReader();

			try
			{
				driverEntities = readDriverEntities(in);
			}
			finally
			{
				IOUtil.close(in);
			}
		}
		else
			driverEntities = new ArrayList<DriverEntity>();

		this.driverEntities = driverEntities;
		this.driverEntityInfoFileLastModified = this.driverEntityInfoFile.lastModified();
	}

	/**
	 * 从输入流中读取{@linkplain DriverEntity}列表。
	 * <p>
	 * 返回结果中不应该包含{@linkplain #isValidDriverEntity(DriverEntity)}为{@code false}的元素。
	 * </p>
	 * 
	 * @param in
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected abstract List<DriverEntity> readDriverEntities(Reader in) throws DriverEntityManagerException;

	/**
	 * 将{@linkplain #driverEntities}列表写入文件。
	 * 
	 * @throws DriverEntityManagerException
	 */
	protected void writeDriverEntities() throws DriverEntityManagerException
	{
		Writer out = getDriverEntityInfoFileWriter();

		try
		{
			writeDriverEntities(out, this.driverEntities);
		}
		finally
		{
			IOUtil.close(out);
		}
	}

	/**
	 * 将{@linkplain DriverEntity}列表写入输出流。
	 * 
	 * @param writer
	 * @param driverEntities
	 * @throws DriverEntityManagerException
	 */
	protected abstract void writeDriverEntities(Writer writer, List<DriverEntity> driverEntities)
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
			return getDriverEntityInfoFileReader(IOUtil.getInputStream(this.driverEntityInfoFile));
		}
		catch (FileNotFoundException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	/**
	 * 获取{@linkplain #driverEntityInfoFile}输入流。
	 * 
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected Reader getDriverEntityInfoFileReader(InputStream in) throws DriverEntityManagerException
	{
		try
		{
			return IOUtil.getReader(in, this.driverEntityFileEncoding);
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
			return getDriverEntityInfoFileWriter(IOUtil.getOutputStream(this.driverEntityInfoFile));
		}
		catch (FileNotFoundException e)
		{
			throw new DriverEntityManagerException(e);
		}
	}

	/**
	 * 获取{@linkplain #driverEntityInfoFile}输出流。
	 * 
	 * @param out
	 * @return
	 * @throws DriverEntityManagerException
	 */
	protected Writer getDriverEntityInfoFileWriter(OutputStream out) throws DriverEntityManagerException
	{
		try
		{
			return IOUtil.getWriter(out, this.driverEntityFileEncoding);
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
		File file = FileUtil.getFile(path, driverLibraryFileName);

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
		File file = getFileInRootDirectory(getDriverLibraryDirectoryName(driverEntityId));

		if (create && !file.exists())
			file.mkdirs();

		return file;
	}

	/**
	 * 获取驱动库的目录名称。
	 * 
	 * @param driverEntityId
	 * @return
	 */
	protected String getDriverLibraryDirectoryName(String driverEntityId)
	{
		return driverEntityId;
	}

	/**
	 * 删除目录下指定名称的文件。
	 * 
	 * @param driverEntityId
	 */
	protected void deleteDriverLibraryDirectory(String driverEntityId)
	{
		File file = getDriverLibraryDirectory(driverEntityId, false);
		FileUtil.deleteFile(file);
	}

	/**
	 * 获取文件。
	 * 
	 * @param directoryName
	 * @param create
	 * @return
	 */
	protected File getFileInRootDirectory(String fileName)
	{
		return FileUtil.getFile(this.rootDirectory, fileName);
	}

	protected static class PathDriverFactoryInfo
	{
		private final PathDriverFactory pathDriverFactory;

		private final long creationModified;

		public PathDriverFactoryInfo(PathDriverFactory pathDriverFactory)
		{
			super();
			this.pathDriverFactory = pathDriverFactory;
			this.creationModified = this.pathDriverFactory.getLastModified();
		}

		public PathDriverFactory getPathDriverFactory()
		{
			return pathDriverFactory;
		}

		public long getCreationModified()
		{
			return creationModified;
		}

		public boolean isModifiedAfterCreation()
		{
			return this.pathDriverFactory.getLastModified() != this.creationModified;
		}

		public long getLastModified()
		{
			return this.pathDriverFactory.getLastModified();
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [pathDriverFactory=" + pathDriverFactory + ", creationModified="
					+ creationModified + "]";
		}
	}
}
