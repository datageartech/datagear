package org.datagear.analysis.support.html;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.ConcurrentChartPluginManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IDUtil;

/**
 * 基于文件目录的{@linkplain ChartPluginManager}。
 * <p>
 * 此类管理指定目录下的{@linkplain HtmlChartPlugin}，并会在文件修改时及时刷新。
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
	private File directory;

	private HtmlChartPluginLoader htmlChartPluginLoader = new HtmlChartPluginLoader();

	private Map<String, PluginFileInfo> chartPluginFileInfoMap = new HashMap<String, PluginFileInfo>();

	public DirectoryHtmlChartPluginManager()
	{
		super();
	}

	public DirectoryHtmlChartPluginManager(File directory)
	{
		super();
		this.directory = directory;
	}

	public DirectoryHtmlChartPluginManager(String directory)
	{
		super();
		this.directory = FileUtil.getDirectory(directory);
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

	public HtmlChartPluginLoader getHtmlChartPluginLoader()
	{
		return htmlChartPluginLoader;
	}

	public void setHtmlChartPluginLoader(HtmlChartPluginLoader htmlChartPluginLoader)
	{
		this.htmlChartPluginLoader = htmlChartPluginLoader;
	}

	protected Map<String, PluginFileInfo> getChartPluginFileInfoMap()
	{
		return chartPluginFileInfoMap;
	}

	protected void setChartPluginFileInfoMap(Map<String, PluginFileInfo> chartPluginFileInfoMap)
	{
		this.chartPluginFileInfoMap = chartPluginFileInfoMap;
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
	public void register(ChartPlugin<?> chartPlugin)
	{
		super.register(chartPlugin);
	}

	@Override
	public ChartPlugin<?> remove(String id)
	{
		return super.remove(id);
	}

	@Override
	public <T extends RenderContext> ChartPlugin<T> get(String id)
	{
		checkForReload();

		return super.get(id);
	}

	@Override
	public <T extends RenderContext> List<ChartPlugin<T>> getAll(Class<? extends T> renderContextType)
	{
		checkForReload();

		return super.getAll(renderContextType);
	}

	@Override
	public List<ChartPlugin<?>> getAll()
	{
		checkForReload();

		return super.getAll();
	}

	@Override
	protected ChartPlugin<?> removeChartPlugin(String id)
	{
		ChartPlugin<?> plugin = super.removeChartPlugin(id);

		PluginFileInfo fileInfo = this.chartPluginFileInfoMap.remove(id);
		if (fileInfo != null)
			FileUtil.deleteFile(fileInfo.getFile());

		return plugin;
	}

	@Override
	protected void removeAllChartPlugins()
	{
		super.removeAllChartPlugins();

		Collection<PluginFileInfo> pluginFileInfos = this.chartPluginFileInfoMap.values();
		for (PluginFileInfo fileInfo : pluginFileInfos)
			FileUtil.deleteFile(fileInfo.getFile());
	}

	protected void checkForReload()
	{
		Set<File> unloads = new HashSet<File>();
		Set<PluginFileInfo> reloads = new HashSet<PluginFileInfo>();

		File[] children = this.directory.listFiles();

		ReadLock readLock = lock.readLock();
		try
		{
			readLock.lock();

			for (Map.Entry<String, PluginFileInfo> entry : this.chartPluginFileInfoMap.entrySet())
			{
				PluginFileInfo fileInfo = entry.getValue();
				if (fileInfo.isModified())
					reloads.add(fileInfo);
			}

			Collection<PluginFileInfo> fileInfos = this.chartPluginFileInfoMap.values();

			for (File child : children)
			{
				boolean loaded = false;

				for (PluginFileInfo fileInfo : fileInfos)
				{
					if (fileInfo.getFile().equals(child))
					{
						loaded = true;
						break;
					}
				}

				if (!loaded)
					unloads.add(child);
			}
		}
		finally
		{
			readLock.unlock();
		}

		if (unloads.isEmpty() && reloads.isEmpty())
			return;

		WriteLock writeLock = this.lock.writeLock();
		try
		{
			for (File unload : unloads)
			{
				ChartPlugin<?> plugin = this.htmlChartPluginLoader.loadFile(unload);

				// 无论是否加载成功，都应该存入PluginFileInfo，避免每次checkForReload()时都会将其当做是未加载的
				this.chartPluginFileInfoMap.put(IDUtil.uuid(), new PluginFileInfo(unload));

				if (plugin != null)
					registerChartPlugin(plugin);
			}

			for (PluginFileInfo reload : reloads)
			{
				ChartPlugin<?> plugin = this.htmlChartPluginLoader.loadFile(reload.getFile());

				if (plugin != null)
					registerChartPlugin(plugin);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected static class PluginFileInfo
	{
		private File file;

		private volatile long lastModified;

		public PluginFileInfo()
		{
			super();
		}

		public PluginFileInfo(File file)
		{
			super();
			this.file = file;
			this.lastModified = resolveLastModified(file);
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

		public long getLastModified()
		{
			return lastModified;
		}

		protected void setLastModified(long lastModified)
		{
			this.lastModified = lastModified;
		}

		public boolean isModified()
		{
			long myModified = resolveLastModified(this.file);

			boolean re = (myModified > this.lastModified);

			this.lastModified = myModified;

			return re;
		}

		protected long resolveLastModified(File file)
		{
			if (!file.exists())
				return 0;

			if (file.isDirectory())
			{
				File[] children = file.listFiles();

				long lastModified = -1;

				for (File child : children)
				{
					long myLastModified = resolveLastModified(child);

					if (myLastModified > lastModified)
						lastModified = myLastModified;
				}

				return lastModified;
			}
			else
				return file.lastModified();
		}
	}
}
