/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.web.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 文件夹工厂。
 * 
 * @author datagear@163.com
 *
 */
public class DirectoryFactory
{
	/** 要建立的子目录名称 */
	private String directoryName;

	/** 如果目录不存在，则创建 */
	private boolean createIfInexistence = true;

	private FileContent[] initFileContents;

	private boolean forceInitFileContents = false;

	private File directory = null;

	public DirectoryFactory()
	{
		super();
	}

	public DirectoryFactory(String directoryName)
	{
		super();
		this.directoryName = directoryName;
	}

	public String getDirectoryName()
	{
		return directoryName;
	}

	/**
	 * 设置要建立的子目录名称。
	 * 
	 * @param directoryName
	 */
	public void setDirectoryName(String directoryName)
	{
		this.directoryName = directoryName;
	}

	public boolean isCreateIfInexistence()
	{
		return createIfInexistence;
	}

	public void setCreateIfInexistence(boolean createIfInexistence)
	{
		this.createIfInexistence = createIfInexistence;
	}

	/**
	 * 是否有要初始创建的文件内容。
	 * 
	 * @return
	 */
	public boolean hasInitFileContents()
	{
		return (this.initFileContents != null && this.initFileContents.length > 0);
	}

	/**
	 * 获取初始创建的文件内容。
	 * 
	 * @return
	 */
	public FileContent[] getInitFileContents()
	{
		return initFileContents;
	}

	/**
	 * 设置初始创建的文件内容。
	 * 
	 * @param initFileContents
	 */
	public void setInitFileContents(FileContent[] initFileContents)
	{
		this.initFileContents = initFileContents;
	}

	/**
	 * 是否强制初始创建文件。
	 * <p>
	 * 强制初始创建文件时，无论文件是否存在，都将重新被创建并写入初始内容。
	 * </p>
	 * 
	 * @return
	 */
	public boolean isForceInitFileContents()
	{
		return forceInitFileContents;
	}

	public void setForceInitFileContents(boolean forceInitFileContents)
	{
		this.forceInitFileContents = forceInitFileContents;
	}

	/**
	 * 初始化。
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException
	{
		this.directory = new File(this.directoryName);

		if (this.createIfInexistence && !this.directory.exists())
			this.directory.mkdirs();

		if (this.initFileContents != null)
		{
			for (FileContent fileContent : this.initFileContents)
			{
				File file = new File(this.directory, fileContent.getName());

				if (this.forceInitFileContents || (!this.forceInitFileContents && !file.exists()))
					writeToFile(file, fileContent.getContent(), fileContent.getEncoding());
			}
		}
	}

	/**
	 * 获取目录。
	 * 
	 * @return
	 */
	public File getDirectory()
	{
		return this.directory;
	}

	/**
	 * 获取目录的绝对路径。
	 * 
	 * @return
	 */
	public String getDirectoryAbsolutePath()
	{
		return this.directory.getAbsolutePath();
	}

	/**
	 * 写文件内容。
	 * 
	 * @param file
	 * @param content
	 *            允许为{@code null}
	 * @param encoding
	 *            允许为{@code null}
	 * @throws IOException
	 */
	protected void writeToFile(File file, String content, String encoding) throws IOException
	{
		BufferedWriter out = null;

		if (encoding != null && !encoding.isEmpty())
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		else
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

		try
		{
			if (content != null)
				out.write(content);
		}
		finally
		{
			out.close();
		}
	}

	/**
	 * 文件名及其内容封装类。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class FileContent
	{
		/** 文件名 */
		private String name;

		/** 文件内容 */
		private String content;

		/** 文件编码 */
		private String encoding;

		public FileContent()
		{
			super();
		}

		public FileContent(String name)
		{
			super();
			this.name = name;
		}

		public FileContent(String name, String content)
		{
			super();
			this.name = name;
			this.content = content;
		}

		public FileContent(String name, String content, String encoding)
		{
			super();
			this.name = name;
			this.content = content;
			this.encoding = encoding;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public String getEncoding()
		{
			return encoding;
		}

		public void setEncoding(String encoding)
		{
			this.encoding = encoding;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [name=" + name + ", content=" + content + ", encoding=" + encoding
					+ "]";
		}
	}
}
