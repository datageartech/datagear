package org.datagear.analysis.support;

import java.io.File;

import org.datagear.analysis.DashboardWidget;
import org.datagear.util.FileUtil;

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
	/** 存储模板文件的文件夹名 */
	private String folderName = "template";

	/** 存储其他资源文件的文件夹名称 */
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
		String path = FileUtil.concatPath(this.folderName, templateName);
		return getRelativePath(id, path);
	}

	/**
	 * 获取模板文件。
	 * 
	 * @param id
	 * @param templateName
	 * @return
	 */
	public File getTemplateFile(String id, String templateName)
	{
		String path = FileUtil.concatPath(this.folderName, templateName);
		return getFile(id, path);
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
		String path = FileUtil.concatPath(this.resFolderName, fileName);
		return getFile(id, path);
	}
}
