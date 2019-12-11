package org.datagear.analysis.support;

import java.io.File;

import org.datagear.analysis.DashboardWidget;

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
	private static final String PATH_SEPARATOR = File.separator;

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
		this.rootDirectory = new File(rootDirectory);
	}

	public File getRootDirectory()
	{
		return rootDirectory;
	}

	public void setRootDirectory(File rootDirectory)
	{
		this.rootDirectory = rootDirectory;
	}

	protected String trimPath(String path)
	{
		if (path == null)
			return null;

		if (PATH_SEPARATOR.equals("\\"))
			return path.replace("/", PATH_SEPARATOR);
		else
			return path.replace("\\", PATH_SEPARATOR);
	}
}
