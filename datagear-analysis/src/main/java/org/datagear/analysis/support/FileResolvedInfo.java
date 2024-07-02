/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.analysis.support;

import java.io.File;

/**
 * 文件及已解析模板信息。
 * 
 * @author datagear@163.com
 *
 */
public class FileResolvedInfo
{
	/** 文件 */
	private File file;

	/** 已解析模板 */
	private String resolvedTemplate = "";

	public FileResolvedInfo()
	{
		super();
	}

	public FileResolvedInfo(File file)
	{
		super();
		this.file = file;
	}

	public FileResolvedInfo(File file, String resolvedTemplate)
	{
		super();
		this.file = file;
		this.resolvedTemplate = resolvedTemplate;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public String getResolvedTemplate()
	{
		return resolvedTemplate;
	}

	public void setResolvedTemplate(String resolvedTemplate)
	{
		this.resolvedTemplate = resolvedTemplate;
	}
}
