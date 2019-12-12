package org.datagear.analysis.support;

import java.io.File;

import org.datagear.analysis.DashboardWidget;
import org.datagear.util.IOUtil;

/**
 * {@linkplain DashboardWidget}文件资源管理器。
 * <p>
 * 此类用于通过{@linkplain DashboardWidget#getId()}来管理{@linkplain DashboardWidget}文件资源。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DashboardWidgetResManager
{
	protected static final String PATH_SEPARATOR = File.separator;

	private File rootDirectory;

	public DashboardWidgetResManager()
	{
		super();
	}

	public DashboardWidgetResManager(File rootDirectory)
	{
		super();
		this.rootDirectory = rootDirectory;
	}

	public DashboardWidgetResManager(String rootDirectory)
	{
		super();
		this.rootDirectory = new File(IOUtil.trimPath(rootDirectory, PATH_SEPARATOR));

		if (!this.rootDirectory.exists())
			this.rootDirectory.mkdirs();
	}

	public File getRootDirectory()
	{
		return rootDirectory;
	}

	public void setRootDirectory(File rootDirectory)
	{
		this.rootDirectory = rootDirectory;

		if (!this.rootDirectory.exists())
			this.rootDirectory.mkdirs();
	}

	/**
	 * 获取相对{@linkplain #getRootDirectory()}的资源路径。
	 * 
	 * @param id
	 * @param subPath
	 * @return
	 */
	public String getRelativePath(String id, String subPath)
	{
		String path = IOUtil.concatPath(id, subPath, PATH_SEPARATOR);
		checkBackwardPath(path);

		return path;
	}

	/**
	 * 获取文件。
	 * <p>
	 * 如果上级目录不存在，则会自动创建。
	 * </p>
	 * 
	 * @param id
	 * @param subPath
	 * @return
	 */
	public File getFile(String id, String subPath)
	{
		if (IOUtil.trimPath(subPath, PATH_SEPARATOR).endsWith(PATH_SEPARATOR))
			return getDirectory(id, subPath);

		String path = getRelativePath(id, subPath);

		int sidx = path.lastIndexOf(PATH_SEPARATOR);

		if (sidx < 0)
			return new File(this.rootDirectory, path);
		else
		{
			String parent = path.substring(0, sidx);

			if (!parent.isEmpty())
			{
				File parentDirectory = new File(this.rootDirectory, parent);

				if (!parentDirectory.exists())
					parentDirectory.mkdirs();

				return new File(parentDirectory, path.substring(sidx + 1));
			}
			else
				return new File(this.rootDirectory, path);
		}
	}

	/**
	 * 获取目录。
	 * <p>
	 * 如果目录不存在，则会自动创建。
	 * </p>
	 * 
	 * @param id
	 * @param subPath
	 * @return
	 */
	public File getDirectory(String id, String subPath)
	{
		String path = getRelativePath(id, subPath);

		File file = new File(this.rootDirectory, path);

		if (!file.exists())
			file.mkdirs();

		return file;
	}

	protected void checkBackwardPath(String path)
	{
		if (containsBackwardPath(path))
			throw new IllegalArgumentException("[.." + PATH_SEPARATOR + "] is not allowed in path [" + path + "]");
	}

	protected boolean containsBackwardPath(String path)
	{
		if (path == null)
			return false;

		path = IOUtil.trimPath(path, PATH_SEPARATOR);

		return (path.indexOf(".." + PATH_SEPARATOR) > -1 || path.indexOf(PATH_SEPARATOR + "..") > -1);
	}
}
