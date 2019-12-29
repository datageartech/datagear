package org.datagear.analysis.support.html;

import java.io.File;
import java.io.IOException;
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
import org.datagear.util.IOUtil;

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
	public Set<HtmlChartPlugin<?>> upload(File file) throws IOException
	{
		Set<HtmlChartPlugin<?>> ids = new HashSet<HtmlChartPlugin<?>>();

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

	protected void upload(File file, Set<HtmlChartPlugin<?>> ids, int depth) throws IOException
	{
		if(depth > 1 || !file.exists())
			return;
		
		if(file.isDirectory())
		{
			if (this.htmlChartPluginLoader.isHtmlChartPluginDirectory(file))
			{
				String name = generateUniquePluginFileName(file.getName());
				File pluginFile = FileUtil.getFile(this.directory, name);
				IOUtil.copy(file, pluginFile);
			}
			else
			{
				File[] children = file.listFiles();
				if (children != null)
				{
					for (File child : children)
						upload(child, ids, depth + 1);
				}
			}
		}
		else if (this.htmlChartPluginLoader.isHtmlChartPluginZip(file))
		{
			String name = generateUniquePluginFileName(file.getName());
			File pluginFile = FileUtil.getFile(this.directory, name);
			IOUtil.copy(file, pluginFile);
		}
		else if (file.getName().toLowerCase().endsWith(".zip"))
		{
			File tmpDirectory = File.createTempFile("dghtmlchartplugin", null);
			tmpDirectory.mkdir();

			IOUtil.unzip(IOUtil.getZipInputStream(file), tmpDirectory);

			upload(tmpDirectory, ids, depth + 1);
		}
	}

	/**
	 * 生成{@linkplain #directory}目录下的唯一插件文件名。
	 * 
	 * @param originName
	 * @return
	 */
	protected String generateUniquePluginFileName(String originName)
	{
		String prefix = originName;
		String ext = "";

		int eidx = originName.lastIndexOf('.');
		if (eidx >= 0)
		{
			prefix = originName.substring(0, eidx);
			ext = originName.substring(eidx);
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
		this.chartPluginFileInfoMap.clear();
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
			writeLock.lock();

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
