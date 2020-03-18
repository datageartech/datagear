/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.io.File;

import org.datagear.persistence.PstParamMapperException;
import org.datagear.util.FileUtil;

/**
 * 指定目录的{@linkplain FilePathValueResolver}。
 * <p>
 * 它限定特定目录下的相对文件路径。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryFilePathValueResolver extends FilePathValueResolver
{
	/** 文件根目录 */
	private File directory;

	public DirectoryFilePathValueResolver()
	{
		super();
	}

	public DirectoryFilePathValueResolver(File directory)
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
	public File getFileValue(String filePathValue) throws PstParamMapperException
	{
		if (!isFilePathValue(filePathValue))
			return null;

		filePathValue = getFilePathContent(filePathValue);

		File file = FileUtil.getFile(this.directory, filePathValue);

		return (file.exists() ? file : null);
	}
}
