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

package org.datagear.analysis.support.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.zip.ZipOutputStream;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginCategoryInfo;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.ConcurrentChartPluginManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于文件目录的{@linkplain ChartPluginManager}。
 * <p>
 * 注意：如果目录内的文件有修改，此类不会自动同步，需要手动调用{@linkplain #refresh()}同步。
 * </p>
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryHtmlChartPluginManager extends ConcurrentChartPluginManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHtmlChartPluginManager.class);

	/** 插件文件主目录 */
	private File directory;

	private HtmlChartPluginLoader htmlChartPluginLoader;

	/** 临时文件目录，用于存放临时文件 */
	private File tmpDirectory = null;

	private Map<String, String> _pluginIdFileNameMap = new HashMap<>();
	private Map<String, FileCheckTime> _fileNameCheckTimeMap = new HashMap<>();

	public DirectoryHtmlChartPluginManager()
	{
		super();
	}

	public DirectoryHtmlChartPluginManager(File directory, HtmlChartPluginLoader htmlChartPluginLoader)
	{
		super();
		this.directory = directory;
		this.htmlChartPluginLoader = htmlChartPluginLoader;
	}

	public DirectoryHtmlChartPluginManager(String directory, HtmlChartPluginLoader htmlChartPluginLoader)
	{
		super();
		this.directory = FileUtil.getDirectory(directory);
		this.htmlChartPluginLoader = htmlChartPluginLoader;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	public void setDirectoryString(String directory)
	{
		this.directory = FileUtil.getDirectory(directory);
	}

	public File getTmpDirectory()
	{
		return tmpDirectory;
	}

	public HtmlChartPluginLoader getHtmlChartPluginLoader()
	{
		return htmlChartPluginLoader;
	}

	public void setHtmlChartPluginLoader(HtmlChartPluginLoader htmlChartPluginLoader)
	{
		this.htmlChartPluginLoader = htmlChartPluginLoader;
	}

	public void setTmpDirectory(File tmpDirectory)
	{
		this.tmpDirectory = tmpDirectory;
	}

	public void setTmpDirectoryString(String tmpDirectory)
	{
		this.tmpDirectory = FileUtil.getDirectory(tmpDirectory);
	}

	protected Map<String, String> getPluginIdFileNameMap()
	{
		return _pluginIdFileNameMap;
	}

	protected void setPluginIdFileNameMap(Map<String, String> pluginIdFileNameMap)
	{
		this._pluginIdFileNameMap = pluginIdFileNameMap;
	}

	protected Map<String, FileCheckTime> getFileNameCheckTimeMap()
	{
		return _fileNameCheckTimeMap;
	}

	protected void setFileNameCheckTimeMap(Map<String, FileCheckTime> fileNameCheckTimeMap)
	{
		this._fileNameCheckTimeMap = fileNameCheckTimeMap;
	}

	/**
	 * 初始化。
	 * <p>
	 * 加载{@linkplain #getDirectory()}中的所有插件。
	 * </p>
	 */
	public void init()
	{
		this.checkForReload();
	}

	/**
	 * 刷新。
	 * <p>
	 * 检查并同步加载{@linkplain #getDirectory()}中的插件。
	 * </p>
	 */
	public void refresh()
	{
		this.checkForReload();
	}

	/**
	 * 上传指定文件所表示的{@linkplain HtmlChartPlugin}。
	 * <p>
	 * 文件可以是单个{@linkplain HtmlChartPlugin}目录，可以是包含多个{@linkplain HtmlChartPlugin}的上级目录，
	 * 可以是单个{@linkplain HtmlChartPlugin}的ZIP，可以是包含多个{@linkplain HtmlChartPlugin}的ZIP。
	 * </p>
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Set<HtmlChartPlugin> upload(File file) throws IOException
	{
		Set<HtmlChartPlugin> ids = new HashSet<>();

		WriteLock writeLock = lock.writeLock();

		try
		{
			writeLock.lock();
			upload(file, ids, 0);
		}
		finally
		{
			writeLock.unlock();
		}

		return ids;
	}

	/**
	 * 下载指定ID的{@linkplain ChartPlugin} ZIP压缩包文件。
	 * 
	 * @throws IOException
	 */
	public void download(ZipOutputStream out, String... ids) throws IOException
	{
		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			File tmpDirectory = createTmpWorkDirectory();

			for (String id : ids)
			{
				File pluginFile = getPluginFile(id);

				if (pluginFile.exists())
					IOUtil.copyInto(pluginFile, tmpDirectory);
			}

			IOUtil.writeFileToZipOutputStream(out, tmpDirectory, "");
		}
		finally
		{
			readLock.unlock();
		}
	}

	protected void upload(File file, Set<HtmlChartPlugin> plugins, int depth) throws IOException
	{
		if (depth > 1 || !file.exists())
			return;

		if (file.isDirectory())
		{
			if (this.htmlChartPluginLoader.isHtmlChartPluginDirectory(file))
			{
				try
				{
					HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadDir(file);
					plugin = registerForUpload(plugin, file);

					if (plugin != null)
						plugins.add(plugin);
				}
				catch (Exception e)
				{
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Load chart plugin from file '" + file.getName() + "' error", e);
				}
			}
			else
			{
				File[] children = file.listFiles();
				if (children != null)
				{
					for (File child : children)
						upload(child, plugins, depth + 1);
				}
			}
		}
		else if (this.htmlChartPluginLoader.isHtmlChartPluginZip(file))
		{
			try
			{
				HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadZip(file);
				plugin = registerForUpload(plugin, file);

				if (plugin != null)
					plugins.add(plugin);
			}
			catch (Exception e)
			{
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Load chart plugin from file '" + file.getName() + "' error", e);
			}
		}
		else if (FileUtil.isExtension(file, "zip"))
		{
			File tmpDirectory = createTmpWorkDirectory();

			IOUtil.unzip(IOUtil.getZipInputStream(file), tmpDirectory);

			File[] children = tmpDirectory.listFiles();
			if (children != null)
			{
				for (File child : children)
					upload(child, plugins, depth + 1);
			}

			FileUtil.deleteFile(tmpDirectory);
		}
	}

	protected HtmlChartPlugin registerForUpload(HtmlChartPlugin uploadPlugin, File uploadPluginFile) throws IOException
	{
		if (!isLegalChartPlugin(uploadPlugin))
			return null;

		HtmlChartPlugin re = null;

		String pluginFileName = uploadPluginFile.getName();

		File sameName = FileUtil.getFile(this.directory, pluginFileName, false);

		// 不存在同名的文件，则拷贝并执行加载
		if (!sameName.exists())
		{
			IOUtil.copyInto(uploadPluginFile, this.directory);
			re = registerHtmlChartPlugin(uploadPlugin, sameName, false);
		}
		else
		{
			String loadedPluginId = getFilePluginId(pluginFileName);
			ChartPlugin loadedPlugin = (loadedPluginId == null ? null : getChartPlugin(loadedPluginId));

			// 同名文件不是插件，则删除它并拷入新文件
			if (loadedPlugin == null)
			{
				FileUtil.deleteFile(sameName);
				IOUtil.copyInto(uploadPluginFile, this.directory);
				re = registerHtmlChartPlugin(uploadPlugin, sameName, false);
			}
			else
			{
				// 同ID的插件，比较版本是否可覆盖
				if (uploadPlugin.getId().equals(loadedPlugin.getId()))
				{
					if (canReplaceForSameId(uploadPlugin, loadedPlugin))
					{
						FileUtil.deleteFile(sameName);
						IOUtil.copyInto(uploadPluginFile, this.directory);
						re = registerHtmlChartPlugin(uploadPlugin, sameName, false);
					}
				}
				// 不同ID的插件，则删除它，载入新的
				else
				{
					removeChartPlugin(loadedPluginId);

					IOUtil.copyInto(uploadPluginFile, this.directory);
					re = registerHtmlChartPlugin(uploadPlugin, sameName, false);
				}
			}
		}

		// 需要重新初始化插件资源，因为文件路径已经改变
		if (re != null)
		{
			this.htmlChartPluginLoader.inflateResources(re, sameName);
		}

		return re;
	}

	@Override
	protected ChartPlugin removeChartPlugin(String id)
	{
		ChartPlugin plugin = super.removeChartPlugin(id);
		deletePluginFile(id);

		return plugin;
	}

	@Override
	protected void removeAllChartPlugins()
	{
		super.removeAllChartPlugins();

		Set<String> pluginIds = this._pluginIdFileNameMap.keySet();
		for (String pluginId : pluginIds)
			deletePluginFile(pluginId);
	}

	protected void deletePluginFile(String pluginId)
	{
		String fileName = this._pluginIdFileNameMap.remove(pluginId);
		File file = FileUtil.getFile(this.directory, fileName, false);
		FileUtil.deleteFile(file);
		this._fileNameCheckTimeMap.remove(fileName);
	}

	protected File getPluginFile(String pluginId)
	{
		String fileName = this._pluginIdFileNameMap.get(pluginId);
		File file = FileUtil.getFile(this.directory, fileName, false);

		return file;
	}

	/**
	 * 获取{@linkplain #directory}下指定文件名对应的插件ID，没有则返回{@code null}。
	 * 
	 * @param fileName
	 * @return
	 */
	protected String getFilePluginId(String fileName)
	{
		for (Map.Entry<String, String> pluginIdFileName : this._pluginIdFileNameMap.entrySet())
		{
			if (fileName.equals(pluginIdFileName.getValue()))
				return pluginIdFileName.getKey();
		}

		return null;
	}

	/**
	 * 检查{@linkplain #directory}目录下的插件文件，如果文件有修改，则重新加载它们对应的插件。
	 */
	protected void checkForReload()
	{
		List<FileCheckTime> reloads = new ArrayList<>();

		// 是否有删除插件文件，如果有删除，那么全部重新加载
		boolean hasDelete = false;

		File[] children = this.directory.listFiles();

		ReadLock readLock = lock.readLock();
		try
		{
			readLock.lock();

			for (Map.Entry<String, FileCheckTime> entry : this._fileNameCheckTimeMap.entrySet())
			{
				FileCheckTime fileCheckTime = entry.getValue();

				if (!fileCheckTime.isFileExists())
				{
					hasDelete = true;
					break;
				}

				if (fileCheckTime.isModified())
				{
					reloads.add(fileCheckTime);

					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Chart plugin file [" + fileCheckTime.getFileName()
								+ "] modified, need reload");
				}
			}

			if (hasDelete)
			{
				for (File child : children)
					reloads.add(new FileCheckTime(child));

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Some chart plugin file deleted, need full reload");
			}
			else
			{
				Collection<FileCheckTime> fileCheckTimes = this._fileNameCheckTimeMap.values();

				for (File child : children)
				{
					boolean loaded = false;

					for (FileCheckTime fileCheckTime : fileCheckTimes)
					{
						if (fileCheckTime.getFile().equals(child))
						{
							loaded = true;
							break;
						}
					}

					if (!loaded)
					{
						FileCheckTime fileCheckTime = new FileCheckTime(child);
						reloads.add(fileCheckTime);

						if (LOGGER.isDebugEnabled())
							LOGGER.debug("Chart plugin file [" + fileCheckTime.getFileName()
									+ "] not loaded, load required");
					}
				}
			}
		}
		finally
		{
			readLock.unlock();
		}

		if (reloads.isEmpty() && !hasDelete)
			return;

		WriteLock writeLock = this.lock.writeLock();
		try
		{
			writeLock.lock();

			if (hasDelete)
			{
				super.removeAllChartPlugins();
				this._pluginIdFileNameMap.clear();
				this._fileNameCheckTimeMap.clear();
			}

			int loadedCount = 0;

			for (FileCheckTime reload : reloads)
			{
				HtmlChartPlugin loadedPlugin = loadAndRegisterHtmlChartPlugin(reload.getFile(), true);

				if (loadedPlugin != null)
					loadedCount++;
			}

			if (LOGGER.isInfoEnabled())
				LOGGER.info("Loaded " + loadedCount + " chart plugins");
		}
		finally
		{
			writeLock.unlock();
		}
	}

	/**
	 * 加载并注册插件，如果注册失败，将返回{@code null}。
	 * 
	 * @param file
	 *            {@linkplain #directory}目录下的一个文件
	 * @return
	 */
	protected HtmlChartPlugin loadAndRegisterHtmlChartPlugin(File file, boolean ignoreCheck)
	{
		try
		{
			HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadFile(file);
			return registerHtmlChartPlugin(plugin, file, ignoreCheck);
		}
		catch (Throwable t)
		{
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Load " + HtmlChartPlugin.class.getSimpleName() + " from file [" + file.getName() + "] error :",
						t);

			return null;
		}
	}

	/**
	 * 注册插件，如果注册失败，将返回{@code null}。
	 * 
	 * @param plugin
	 * @param file
	 *            {@linkplain #directory}目录下的一个文件
	 * @param ignoreCheck
	 * @return
	 */
	protected HtmlChartPlugin registerHtmlChartPlugin(HtmlChartPlugin plugin, File file, boolean ignoreCheck)
	{
		String fileName = file.getName();

		if (!isLegalChartPlugin(plugin))
			plugin = null;
		else
		{
			if (registerChartPlugin(plugin, ignoreCheck))
			{
				inflateCagetory(plugin);
				this._pluginIdFileNameMap.put(plugin.getId(), fileName);
			}
			else
				plugin = null;
		}

		this._fileNameCheckTimeMap.put(fileName, new FileCheckTime(file));

		return plugin;
	}

	protected void inflateCagetory(HtmlChartPlugin plugin)
	{
		if (plugin == null)
			return;

		List<ChartPluginCategoryInfo> categoryInfos = plugin.getCategoryInfos();

		if (categoryInfos == null)
			return;

		for (int i = 0; i < categoryInfos.size(); i++)
		{
			ChartPluginCategoryInfo categoryInfo = categoryInfos.get(i);
			Category category = categoryInfo.getCategory();
			String categoryName = (category == null ? null : category.getName());

			Map<String, ChartPlugin> map = getChartPluginMap();

			// 如果类别定义了更详细的信息，则全部替换为使用它
			if (category.hasNameLabel())
			{
				for (ChartPlugin chartPlugin : map.values())
				{
					if (!(chartPlugin instanceof AbstractChartPlugin))
						continue;

					List<ChartPluginCategoryInfo> myCategoryInfos = chartPlugin.getCategoryInfos();

					if (myCategoryInfos == null)
						continue;

					for (int j = 0; j < myCategoryInfos.size(); j++)
					{
						ChartPluginCategoryInfo myCategoryInfo = myCategoryInfos.get(j);
						Category myCategory = myCategoryInfo.getCategory();

						if (myCategory != null && myCategory != category
								&& StringUtil.isEquals(myCategory.getName(), categoryName))
						{
							myCategoryInfo.setCategory(category);
						}
					}
				}
			}
			// 否则，查找并使用定义详细信息的类别
			else
			{
				for (ChartPlugin chartPlugin : map.values())
				{
					List<ChartPluginCategoryInfo> myCategoryInfos = chartPlugin.getCategoryInfos();

					if (myCategoryInfos == null)
						continue;

					Category fullCategory = null;

					for (ChartPluginCategoryInfo myCategoryInfo : myCategoryInfos)
					{
						Category myCategory = myCategoryInfo.getCategory();

						if (myCategory != null && myCategory != category && myCategory.hasNameLabel()
								&& StringUtil.isEquals(myCategory.getName(), categoryName))
						{
							fullCategory = myCategory;
							break;
						}
					}

					if (fullCategory != null)
					{
						categoryInfo.setCategory(fullCategory);
						break;
					}
				}
			}
		}
	}

	/**
	 * 生成{@linkplain #directory}目录下的唯一插件文件名。
	 * 
	 * @param originPluginFile
	 * @return
	 */
	protected String generateUniquePluginFileName(File originPluginFile)
	{
		String originName = originPluginFile.getName();

		String prefix = originName;
		String ext = "";

		if (!originPluginFile.isDirectory())
		{
			int eidx = originName.lastIndexOf('.');
			if (eidx >= 0)
			{
				prefix = originName.substring(0, eidx);
				ext = originName.substring(eidx);
			}
		}

		String name = prefix + ext;
		for (int i = 1;; i++)
		{
			File file = FileUtil.getFile(this.directory, name);
			if (!file.exists())
				break;

			name = prefix + "_" + i + ext;
		}

		return name;
	}

	protected File createTmpWorkDirectory() throws IOException
	{
		if (this.tmpDirectory != null)
			return FileUtil.generateUniqueDirectory(this.tmpDirectory);
		else
			return FileUtil.createTempDirectory();
	}

	/**
	 * 文件加载插件的检查时间。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class FileCheckTime
	{
		private final File file;
		private volatile long lastModified;

		public FileCheckTime(File file)
		{
			super();
			this.file = file;
			this.lastModified = resolveLastModified(this.file);
		}

		public File getFile()
		{
			return file;
		}

		public String getFileName()
		{
			return this.file.getName();
		}

		public boolean isFileExists()
		{
			return this.file.exists();
		}

		public boolean isModified()
		{
			long fileModified = resolveLastModified(this.file);
			boolean modified = (fileModified != this.lastModified);
			this.lastModified = fileModified;

			return modified;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [file=" + file.getName() + ", lastModified=" + lastModified + "]";
		}

		protected long resolveLastModified(File file)
		{
			if (!file.exists())
				return 0;

			long lastModified = file.lastModified();

			if (file.isDirectory())
			{
				File[] children = file.listFiles();

				for (File child : children)
				{
					long myLastModified = resolveLastModified(child);

					if (myLastModified > lastModified)
						lastModified = myLastModified;
				}

				return lastModified;
			}
			else
				return lastModified;
		}
	}
}
