package org.datagear.analysis.support;

import java.io.File;

import org.datagear.analysis.DashboardWidget;
import org.datagear.util.FileUtil;

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
		this.rootDirectory = FileUtil.getDirectory(FileUtil.trimPath(rootDirectory));
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
		String path = FileUtil.concatPath(id, subPath);

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
		if (FileUtil.trimPath(subPath).endsWith(FileUtil.PATH_SEPARATOR))
			return getDirectory(id, subPath);

		String path = getRelativePath(id, subPath);

		int sidx = path.lastIndexOf(FileUtil.PATH_SEPARATOR);

		if (sidx < 0)
			return FileUtil.getFile(this.rootDirectory, path);
		else
		{
			String parent = path.substring(0, sidx);

			if (!parent.isEmpty())
			{
				File parentDirectory = FileUtil.getDirectory(this.rootDirectory, parent);

				return FileUtil.getFile(parentDirectory, path.substring(sidx + 1));
			}
			else
				return FileUtil.getFile(this.rootDirectory, path);
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

		File file = FileUtil.getDirectory(this.rootDirectory, path);

		return file;
	}
}
