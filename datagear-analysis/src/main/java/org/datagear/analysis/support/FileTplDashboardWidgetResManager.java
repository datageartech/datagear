/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.Dashboard;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;

/**
 * 基于文件的{@linkplain TplDashboardWidgetResManager}。
 * 
 * @author datagear@163.com
 *
 */
public class FileTplDashboardWidgetResManager extends AbstractTplDashboardWidgetResManager
{
	private File rootDirectory;

	public FileTplDashboardWidgetResManager()
	{
		super();
	}

	public FileTplDashboardWidgetResManager(File rootDirectory)
	{
		super();
		this.rootDirectory = rootDirectory;
	}

	public FileTplDashboardWidgetResManager(String rootDirectory)
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
	 * @param id
	 *            {@linkplain Dashboard#getId()}
	 * @param name
	 *            资源名称
	 * @return
	 */
	public String getRelativePath(String id, String name)
	{
		return doGetRelativePath(id, name);
	}

	@Override
	public boolean exists(String id, String name)
	{
		File file = getFile(id, name, false);
		return file.exists();
	}

	@Override
	public InputStream getInputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name, false);
		return IOUtil.getInputStream(file);
	}

	@Override
	public OutputStream getOutputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name, true);
		return IOUtil.getOutputStream(file);
	}

	@Override
	public void copyFrom(String id, File directory) throws IOException
	{
		File myDirectory = FileUtil.getDirectory(this.rootDirectory, id);
		IOUtil.copy(directory, myDirectory, false);
	}

	@Override
	public void copyTo(String id, File directory) throws IOException
	{
		File myDirectory = FileUtil.getDirectory(this.rootDirectory, id);
		IOUtil.copy(myDirectory, directory, false);
	}

	@Override
	public void copyTo(String sourceId, String targetId) throws IOException
	{
		File sourceDirectory = FileUtil.getDirectory(this.rootDirectory, sourceId);
		File targetDirectory = FileUtil.getDirectory(this.rootDirectory, targetId);

		IOUtil.copy(sourceDirectory, targetDirectory, false);
	}

	@Override
	public long lastModified(String id, String name)
	{
		File file = getFile(id, name, false);
		return file.lastModified();
	}

	@Override
	public List<String> list(String id)
	{
		File directory = FileUtil.getDirectory(this.rootDirectory, id, false);

		if (!directory.exists())
			return new ArrayList<>(0);

		List<File> files = new ArrayList<>();
		listAllDescendentFiles(directory, files);

		List<String> resources = new ArrayList<>(files.size());

		for (File file : files)
		{
			String resource = trimResourceName(directory, file);
			resources.add(resource);
		}

		return resources;
	}

	@Override
	public void delete(String id)
	{
		File directory = FileUtil.getDirectory(this.rootDirectory, id);
		FileUtil.deleteFile(directory);
	}

	@Override
	public void delete(String id, String name)
	{
		File file = getFile(id, name, false);
		FileUtil.deleteFile(file);
	}

	@Override
	public Map<String, String> rename(String id, String srcName, String destName) throws IOException
	{
		File idRoot = FileUtil.getDirectory(this.rootDirectory, id);
		
		if(!idRoot.exists())
			return Collections.emptyMap();
		
		File srcFile = FileUtil.getFile(idRoot, srcName);
		
		if(!srcFile.exists())
			return Collections.emptyMap();
		
		File destFile = FileUtil.getFile(idRoot, destName, true);
		
		Map<String, String> renames = renameResourceFile(idRoot, srcFile, destFile);
		return renames;
	}
	
	protected Map<String, String> renameResourceFile(File idRoot, File srcFile, File destFile) throws IOException
	{
		Map<String, String> renames = new HashMap<String, String>();
		
		Map<File, File> tracks = FileUtil.renameTracked(srcFile, destFile);
		for(Map.Entry<File, File> track : tracks.entrySet())
		{
			File dest = track.getKey();
			File src = track.getValue();
			
			if(dest.isDirectory())
				continue;
			
			renames.put(trimResourceName(idRoot, src), trimResourceName(idRoot, dest));
		}
		
		return renames;
	}

	protected void listAllDescendentFiles(File directory, List<File> files)
	{
		if (!directory.exists())
			return;

		File[] children = directory.listFiles();

		Arrays.sort(children, new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				return o1.getName().compareTo(o2.getName());
			}
		});

		for (File child : children)
		{
			files.add(child);

			if (child.isDirectory())
				listAllDescendentFiles(child, files);
		}
	}
	
	protected String trimResourceName(File idDirectory, File resource)
	{
		String resourceName = FileUtil.getRelativePath(idDirectory, resource);
		resourceName = FileUtil.trimPath(resourceName, FileUtil.PATH_SEPARATOR_SLASH);
		
		if (resource.isDirectory() && !resourceName.endsWith(FileUtil.PATH_SEPARATOR_SLASH))
			resourceName += FileUtil.PATH_SEPARATOR_SLASH;

		return resourceName;
	}

	protected File getFile(String id, String name, boolean create)
	{
		String path = doGetRelativePath(id, name);
		return FileUtil.getFile(this.rootDirectory, path, create);
	}

	protected String doGetRelativePath(String id, String name)
	{
		String path = FileUtil.concatPath(id, name);
		return path;
	}
}
