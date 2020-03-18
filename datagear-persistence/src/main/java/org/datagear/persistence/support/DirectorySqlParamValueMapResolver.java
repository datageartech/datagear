/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.File;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.SqlParamValueMapperException;
import org.datagear.util.FileUtil;

/**
 * 指定目录的{@linkplain SqlParamValueMapResolver}。
 * <p>
 * 它限定特定目录下的相对文件路径。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DirectorySqlParamValueMapResolver extends SqlParamValueMapResolver
{
	/** 文件根目录 */
	private File directory;

	public DirectorySqlParamValueMapResolver()
	{
		super();
	}

	public DirectorySqlParamValueMapResolver(File directory)
	{
		super();
		this.directory = directory;
	}

	public File getDirectory()
	{
		return directory;
	}

	public void setDirectory(File directory)
	{
		this.directory = directory;
	}

	@Override
	public File getFile(Table table, Column column, String value) throws SqlParamValueMapperException
	{
		if (!isFile(value))
			return null;

		value = getFilePath(value);

		File file = FileUtil.getFile(this.directory, value);

		return (file.exists() ? file : null);
	}
}
