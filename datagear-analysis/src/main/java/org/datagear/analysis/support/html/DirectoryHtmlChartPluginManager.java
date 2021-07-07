/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.support.AbstractChartPlugin;
import org.datagear.analysis.support.ConcurrentChartPluginManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于文件目录的{@linkplain ChartPluginManager}。
 * <p>
 * 此类管理指定目录下符合{@linkplain HtmlChartPluginLoader}规范的{@linkplain HtmlChartPlugin}，并会在文件修改时及时刷新。
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

	private long readCheckForReloadTimeThreashold = (LOGGER.isDebugEnabled() ? 0 : 5 * 60 * 1000);

	private Map<String, String> pluginIdFileNameMap = new HashMap<>();

	private Map<String, FileCheckTime> fileNameCheckTimeMap = new HashMap<>();

	private volatile long _prevReadCheckForReloadTime = 0;

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

	public long getReadCheckForReloadTimeThreashold()
	{
		return readCheckForReloadTimeThreashold;
	}

	public void setReadCheckForReloadTimeThreashold(long readCheckForReloadTimeThreashold)
	{
		this.readCheckForReloadTimeThreashold = readCheckForReloadTimeThreashold;
	}

	protected Map<String, String> getPluginIdFileNameMap()
	{
		return pluginIdFileNameMap;
	}

	protected void setPluginIdFileNameMap(Map<String, String> pluginIdFileNameMap)
	{
		this.pluginIdFileNameMap = pluginIdFileNameMap;
	}

	protected Map<String, FileCheckTime> getFileNameCheckTimeMap()
	{
		return fileNameCheckTimeMap;
	}

	protected void setFileNameCheckTimeMap(Map<String, FileCheckTime> fileNameCheckTimeMap)
	{
		this.fileNameCheckTimeMap = fileNameCheckTimeMap;
	}

	/**
	 * 初始化。
	 */
	public void init()
	{
		this.checkForReload();
	}

	/**
	 * 刷新。
	 */
	public void refresh()
	{
		this.checkForReload();
	}

	@Override
	public ChartPlugin get(String id)
	{
		readCheckForReload();

		return super.get(id);
	}

	@Override
	public <T extends ChartPlugin> List<T> getAll(Class<? super T> renderContextType)
	{
		readCheckForReload();

		return super.getAll(renderContextType);
	}

	@Override
	public List<ChartPlugin> getAll()
	{
		readCheckForReload();

		return super.getAll();
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
		readCheckForReload();

		ReadLock readLock = this.lock.readLock();

		try
		{
			readLock.lock();

			File tmpDirectory = createTmpWorkDirectory();

			for (String id : ids)
			{
				File pluginFile = getPluginFile(id);

				if (pluginFile.exists())
					IOUtil.copy(pluginFile, tmpDirectory, true);
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
			HtmlChartPlugin myPlugin = this.htmlChartPluginLoader.load(file);

			if (myPlugin != null)
			{
				myPlugin = registerForUpload(myPlugin, file);
				if (myPlugin != null)
					plugins.add(myPlugin);
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
			HtmlChartPlugin myPlugin = this.htmlChartPluginLoader.loadZip(file);

			if (myPlugin != null)
			{
				myPlugin = registerForUpload(myPlugin, file);
				if (myPlugin != null)
					plugins.add(myPlugin);
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

		String pluginFileName = uploadPluginFile.getName();

		File sameName = FileUtil.getFile(this.directory, pluginFileName, false);

		// 不存在同名的文件，则拷贝并执行加载
		if (!sameName.exists())
		{
			IOUtil.copy(uploadPluginFile, this.directory, true);
			return registerHtmlChartPlugin(uploadPlugin, sameName);
		}
		else
		{
			String loadedPluginId = getFilePluginId(pluginFileName);
			ChartPlugin loadedPlugin = (loadedPluginId == null ? null : getChartPlugin(loadedPluginId));

			// 同名文件不是插件，则删除它并拷入新文件
			if (loadedPlugin == null)
			{
				FileUtil.deleteFile(sameName);
				IOUtil.copy(uploadPluginFile, this.directory, true);
				return registerHtmlChartPlugin(uploadPlugin, sameName);
			}
			else
			{
				// 同ID的插件，比较版本是否可覆盖
				if (uploadPlugin.getId().equals(loadedPlugin.getId()))
				{
					if (canReplaceForSameId(uploadPlugin, loadedPlugin))
					{
						FileUtil.deleteFile(sameName);
						IOUtil.copy(uploadPluginFile, this.directory, true);
						return registerHtmlChartPlugin(uploadPlugin, sameName);
					}
					else
						return null;
				}
				// 不同ID的插件，则删除它，载入新的
				else
				{
					removeChartPlugin(loadedPluginId);

					IOUtil.copy(uploadPluginFile, this.directory, true);
					return registerHtmlChartPlugin(uploadPlugin, sameName);
				}
			}
		}
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

		Set<String> pluginIds = this.pluginIdFileNameMap.keySet();
		for (String pluginId : pluginIds)
			deletePluginFile(pluginId);
	}

	protected void deletePluginFile(String pluginId)
	{
		String fileName = this.pluginIdFileNameMap.remove(pluginId);
		File file = FileUtil.getFile(this.directory, fileName, false);
		FileUtil.deleteFile(file);
		this.fileNameCheckTimeMap.remove(fileName);
	}

	protected File getPluginFile(String pluginId)
	{
		String fileName = this.pluginIdFileNameMap.get(pluginId);
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
		for (Map.Entry<String, String> pluginIdFileName : this.pluginIdFileNameMap.entrySet())
		{
			if (fileName.equals(pluginIdFileName.getValue()))
				return pluginIdFileName.getKey();
		}

		return null;
	}

	/**
	 * 读取操作检查加载。
	 * 
	 * @return
	 */
	protected boolean readCheckForReload()
	{
		long currentTime = System.currentTimeMillis();

		// 不需频繁重新检查
		if (currentTime - this._prevReadCheckForReloadTime < this.readCheckForReloadTimeThreashold)
			return false;

		this._prevReadCheckForReloadTime = currentTime;
		checkForReload();

		return true;
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

			for (Map.Entry<String, FileCheckTime> entry : this.fileNameCheckTimeMap.entrySet())
			{
				FileCheckTime fileCheckTime = entry.getValue();

				if (!fileCheckTime.isFileExists())
				{
					hasDelete = true;
					break;
				}

				if (fileCheckTime.isTimeout())
					reloads.add(fileCheckTime);

			}

			if (hasDelete)
			{
				for (File child : children)
					reloads.add(new FileCheckTime(child));
			}
			else
			{
				Collection<FileCheckTime> fileCheckTimes = this.fileNameCheckTimeMap.values();

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
						reloads.add(new FileCheckTime(child));
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
				this.pluginIdFileNameMap.clear();
				this.fileNameCheckTimeMap.clear();
			}

			for (FileCheckTime reload : reloads)
				loadAndRegisterHtmlChartPlugin(reload.getFile());
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
	protected HtmlChartPlugin loadAndRegisterHtmlChartPlugin(File file)
	{
		try
		{
			HtmlChartPlugin plugin = this.htmlChartPluginLoader.loadFile(file);
			return registerHtmlChartPlugin(plugin, file);
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
	 * @return
	 */
	protected HtmlChartPlugin registerHtmlChartPlugin(HtmlChartPlugin plugin, File file)
	{
		String fileName = file.getName();

		if (!isLegalChartPlugin(plugin))
			plugin = null;
		else
		{
			if (registerChartPlugin(plugin))
			{
				inflateCagetory(plugin);
				this.pluginIdFileNameMap.put(plugin.getId(), fileName);
			}
			else
				plugin = null;
		}

		this.fileNameCheckTimeMap.put(fileName, new FileCheckTime(file));

		return plugin;
	}

	@Override
	protected boolean canReplaceForSameId(ChartPlugin my, Version myVersion, ChartPlugin old, Version oldVersion)
	{
		// 调试模式下总替换
		if (LOGGER.isDebugEnabled())
			return true;

		return super.canReplaceForSameId(my, myVersion, old, oldVersion);
	}

	protected void inflateCagetory(HtmlChartPlugin plugin)
	{
		if (plugin == null)
			return;

		Category category = plugin.getCategory();

		if (category == null)
			return;

		String categoryName = category.getName();

		Map<String, ChartPlugin> map = getChartPluginMap();

		// 如果类别定义了更详细的信息，则全部替换为使用它
		if (category.hasNameLabel())
		{
			for (ChartPlugin chartPlugin : map.values())
			{
				if (!(chartPlugin instanceof AbstractChartPlugin))
					continue;

				Category myCategory = chartPlugin.getCategory();

				if (myCategory != null && myCategory != category
						&& StringUtil.isEquals(myCategory.getName(), categoryName))
				{
					((AbstractChartPlugin) chartPlugin).setCategory(category);
				}
			}
		}
		// 否则，查找并使用定义详细信息的类别
		else
		{
			for (ChartPlugin chartPlugin : map.values())
			{
				Category myCategory = chartPlugin.getCategory();

				if (myCategory != null && myCategory != category && myCategory.hasNameLabel()
						&& StringUtil.isEquals(myCategory.getName(), categoryName))
				{
					plugin.setCategory(myCategory);
					break;
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

	@Override
	protected boolean isLegalChartPlugin(ChartPlugin chartPlugin)
	{
		boolean legal = super.isLegalChartPlugin(chartPlugin);

		if (legal)
		{
			if (chartPlugin instanceof HtmlChartPlugin)
			{
				HtmlChartPlugin htmlChartPlugin = (HtmlChartPlugin) chartPlugin;

				if (htmlChartPlugin.getChartRenderer() == null)
					legal = false;
			}
		}

		return legal;
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
		private File file;
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

		public void setFile(File file)
		{
			this.file = file;
			this.lastModified = resolveLastModified(file);
		}

		public boolean isFileExists()
		{
			return this.file.exists();
		}

		public boolean isTimeout()
		{
			long fileModified = resolveLastModified(this.file);

			boolean timeout = (fileModified > this.lastModified);

			this.lastModified = fileModified;

			return timeout;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [file=" + file + ", lastModified=" + lastModified + "]";
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
