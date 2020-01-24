package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.datagear.analysis.Dashboard;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 基于文件的{@linkplain DashboardWidgetResManager}。
 * 
 * @author datagear@163.com
 *
 */
public class FileDashboardWidgetResManager extends AbstractDashboardWidgetResManager
{
	private File rootDirectory;

	public FileDashboardWidgetResManager()
	{
		super();
	}

	public FileDashboardWidgetResManager(File rootDirectory)
	{
		super();
		this.rootDirectory = rootDirectory;
	}

	public FileDashboardWidgetResManager(String rootDirectory)
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
	 * 获取指定资源相对{@linkplain #getRootDirectory()}的路径。
	 * 
	 * @param id   {@linkplain Dashboard#getId()}
	 * @param name 资源名称
	 * @return
	 */
	public String getRelativePath(String id, String name)
	{
		return doGetRelativePath(id, name);
	}

	@Override
	public InputStream getInputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name);
		return IOUtil.getInputStream(file);
	}

	@Override
	public OutputStream getOutputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name);
		return IOUtil.getOutputStream(file);
	}

	@Override
	public void copyFrom(String id, File directory) throws IOException
	{
		File myDirectory = FileUtil.getDirectory(this.rootDirectory, id);
		IOUtil.copy(directory, myDirectory, false);
	}

	@Override
	public boolean contains(String id, String name)
	{
		File file = getFile(id, name);
		return file.exists();
	}

	@Override
	public long lastModified(String id, String name)
	{
		File file = getFile(id, name);
		return file.lastModified();
	}

	@Override
	public void delete(String id)
	{
		File directory = FileUtil.getDirectory(this.rootDirectory, id);
		FileUtil.deleteFile(directory);
	}

	protected File getFile(String id, String name)
	{
		String path = doGetRelativePath(id, name);
		return FileUtil.getFile(this.rootDirectory, path);
	}

	protected String doGetRelativePath(String id, String name)
	{
		String path = FileUtil.concatPath(id, name);
		return path;
	}
}
