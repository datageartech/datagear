package org.datagear.analysis.support;

import java.io.File;

import org.datagear.analysis.DashboardWidget;
import org.datagear.util.IOUtil;

/**
 * 模板{@linkplain DashboardWidgetResManager}。
 * <p>
 * 此类根据{@linkplain DashboardWidget#getId()}分类管理模板文件和资源文件。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class TemplateDashboardWidgetResManager extends DashboardWidgetResManager
{
	private String folderName = "template";

	private String resFolderName = "resource";

	public TemplateDashboardWidgetResManager()
	{
		super();
	}

	public TemplateDashboardWidgetResManager(File rootDirectory)
	{
		super(rootDirectory);
	}

	public TemplateDashboardWidgetResManager(String rootDirectory)
	{
		super(rootDirectory);
	}

	public String getFolderName()
	{
		return folderName;
	}

	public void setFolderName(String folderName)
	{
		this.folderName = folderName;
	}

	public String getResFolderName()
	{
		return resFolderName;
	}

	public void setResFolderName(String resFolderName)
	{
		this.resFolderName = resFolderName;
	}

	/**
	 * 获取模板文件相对{@linkplain #getRootDirectory()}的路径。
	 * 
	 * @param id
	 * @param templateName
	 * @return
	 */
	public String getTemplateRelativePath(String id, String templateName)
	{
		String path = IOUtil.concatPath(this.folderName, templateName, PATH_SEPARATOR);
		return getRelativePath(id, path);
	}

	/**
	 * 获取资源文件。
	 * 
	 * @param id
	 * @param fileName
	 * @return
	 */
	public File getResFile(String id, String fileName)
	{
		String path = IOUtil.concatPath(this.resFolderName, fileName, PATH_SEPARATOR);
		return getFile(id, path);
	}
}
