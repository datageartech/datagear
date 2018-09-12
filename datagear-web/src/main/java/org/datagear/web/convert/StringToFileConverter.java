/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.convert;

import java.io.File;

import org.springframework.core.convert.converter.Converter;

/**
 * 字符串至文件转换器。
 * <p>
 * 此类将给定字符串视作{@linkplain #getDirectory()}目录下的相对路径，并以此转换为{@linkplain File}对象。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class StringToFileConverter implements Converter<String, File>
{
	private File directory;

	public StringToFileConverter()
	{
		super();
	}

	public StringToFileConverter(File directory)
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
	public File convert(String source)
	{
		File file = new File(this.directory, source);

		return file;
	}
}
