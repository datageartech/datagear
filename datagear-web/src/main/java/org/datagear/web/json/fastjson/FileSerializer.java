/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.json.fastjson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

/**
 * 文件{@linkplain ObjectSerializer}。
 * <p>
 * 它将指定目录下的文件序列化为相对路径名。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class FileSerializer implements ObjectSerializer
{
	private File directory;

	private boolean deleteHeadSeparator;

	public FileSerializer()
	{
		super();
	}

	public FileSerializer(File directory)
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

	public boolean isDeleteHeadSeparator()
	{
		return deleteHeadSeparator;
	}

	public void setDeleteHeadSeparator(boolean deleteHeadSeparator)
	{
		this.deleteHeadSeparator = deleteHeadSeparator;
	}

	@Override
	public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
			throws IOException
	{
		String str = null;

		if (object != null)
		{
			File file = (File) object;

			String path = file.getPath();
			String directoryPath = this.directory.getPath();

			if (!path.startsWith(directoryPath))
				throw new IllegalArgumentException("[" + file + "] is not in [" + this.directory + "]");

			str = path.substring(directoryPath.length());

			if (this.deleteHeadSeparator && str.startsWith(File.separator))
				str = str.substring(File.separator.length());
		}

		serializer.write(str);
	}
}
