package org.datagear.analysis.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.datagear.analysis.Dashboard;
import org.datagear.analysis.TemplateDashboardWidget;
import org.datagear.analysis.TemplateDashboardWidgetResManager;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 基于文件的{@linkplain TemplateDashboardWidgetResManager}。
 * <p>
 * 此类将{@linkplain TemplateDashboardWidget#getTemplate()}作为资源文件名处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FileTemplateDashboardWidgetResManager extends AbstractTemplateDashboardWidgetResManager
{
	private File rootDirectory;

	private boolean templateAsContent = false;

	public FileTemplateDashboardWidgetResManager()
	{
		super();
	}

	public FileTemplateDashboardWidgetResManager(File rootDirectory)
	{
		super();
		this.rootDirectory = rootDirectory;
	}

	public FileTemplateDashboardWidgetResManager(String rootDirectory)
	{
		super();
		this.rootDirectory = FileUtil.getDirectory(FileUtil.trimPath(rootDirectory));
	}

	/**
	 * 是否将{@linkplain TemplateDashboardWidget#getTemplate()}作为模板内容而非模板资源名处理。
	 * 
	 * @return
	 */
	public boolean isTemplateAsContent()
	{
		return templateAsContent;
	}

	public void setTemplateAsContent(boolean templateAsContent)
	{
		this.templateAsContent = templateAsContent;
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
	public Reader getTemplateReader(TemplateDashboardWidget<?> widget, String template) throws IOException
	{
		if (this.templateAsContent)
		{
			String content = template;
			if (StringUtil.isEmpty(content))
				content = "";

			return IOUtil.getReader(content);
		}
		else
		{
			String name = getResourceNameForTemplate(widget, template);
			return getResourceReader(widget.getId(), name, getTemplateEncodingWithDefault(widget));
		}
	}

	@Override
	public Writer getTemplateWriter(TemplateDashboardWidget<?> widget, String template) throws IOException
	{
		if (this.templateAsContent)
			throw new UnsupportedOperationException();
		else
		{
			String name = getResourceNameForTemplate(widget, template);
			return getResourceWriter(widget.getId(), name, getTemplateEncodingWithDefault(widget));
		}
	}

	@Override
	public InputStream getResourceInputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name, false);

		if (!file.exists())
			return new ByteArrayInputStream(new byte[0]);
		else
			return IOUtil.getInputStream(file);
	}

	@Override
	public OutputStream getResourceOutputStream(String id, String name) throws IOException
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
	public boolean containsResource(String id, String name)
	{
		File file = getFile(id, name, false);
		return file.exists();
	}

	@Override
	public long lastModifiedResource(String id, String name)
	{
		File file = getFile(id, name, false);
		return file.lastModified();
	}

	@Override
	public List<String> listResources(String id)
	{
		File directory = FileUtil.getDirectory(this.rootDirectory, id, false);

		if (!directory.exists())
			return new ArrayList<String>(0);

		List<File> files = new ArrayList<File>();
		listAllDescendentFiles(directory, files);

		List<String> resources = new ArrayList<String>(files.size());

		for (File file : files)
		{
			String resource = FileUtil.getRelativePath(directory, file);

			resource = FileUtil.trimPath(resource, FileUtil.PATH_SEPARATOR_SLASH);
			if (file.isDirectory())
				resource += FileUtil.PATH_SEPARATOR_SLASH;

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

	/**
	 * 获取模板的资源名。
	 * 
	 * @param widget
	 * @param template
	 * @return
	 */
	protected String getResourceNameForTemplate(TemplateDashboardWidget<?> widget, String template)
	{
		return template;
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
