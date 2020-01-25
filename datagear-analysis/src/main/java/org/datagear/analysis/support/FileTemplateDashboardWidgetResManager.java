package org.datagear.analysis.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.datagear.analysis.Dashboard;
import org.datagear.analysis.TemplateDashboardWidget;
import org.datagear.util.FileUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.StringUtil;

/**
 * 基于文件的{@linkplain DashboardWidgetResManager}。
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
	 * @param id   {@linkplain Dashboard#getId()}
	 * @param name 资源名称
	 * @return
	 */
	public String getRelativePath(String id, String name)
	{
		return doGetRelativePath(id, name);
	}

	@Override
	public Reader getTemplateReader(TemplateDashboardWidget<?> widget) throws IOException
	{
		if (this.templateAsContent)
		{
			String content = widget.getTemplate();
			if (StringUtil.isEmpty(content))
				content = "";

			return IOUtil.getReader(content);
		}
		else
		{
			String name = getResourceNameForTemplate(widget);
			return getResourceReader(widget.getId(), name, getTemplateEncodingWithDefault(widget));
		}
	}

	@Override
	public Writer getTemplateWriter(TemplateDashboardWidget<?> widget) throws IOException
	{
		if (this.templateAsContent)
			throw new UnsupportedOperationException();
		else
		{
			String name = getResourceNameForTemplate(widget);
			return getResourceWriter(widget.getId(), name, getTemplateEncodingWithDefault(widget));
		}
	}

	@Override
	public InputStream getResourceInputStream(String id, String name) throws IOException
	{
		File file = getFile(id, name);
		return IOUtil.getInputStream(file);
	}

	@Override
	public OutputStream getResourceOutputStream(String id, String name) throws IOException
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
	public boolean containsResource(String id, String name)
	{
		File file = getFile(id, name);
		return file.exists();
	}

	@Override
	public long lastModifiedResource(String id, String name)
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

	/**
	 * 获取{@linkplain TemplateDashboardWidget#getTemplate()}的资源名。
	 * 
	 * @param widget
	 * @return
	 */
	protected String getResourceNameForTemplate(TemplateDashboardWidget<?> widget)
	{
		return widget.getTemplate();
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
