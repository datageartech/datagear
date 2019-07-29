/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.InputStream;

import org.datagear.util.IOUtil;

/**
 * 类路径输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class ClasspathInputStreamResourceFactory implements ResourceFactory<InputStream>
{
	private String classpath;

	public ClasspathInputStreamResourceFactory()
	{
		super();
	}

	public ClasspathInputStreamResourceFactory(String classpath)
	{
		super();
		this.classpath = classpath;
	}

	public String getClasspath()
	{
		return classpath;
	}

	public void setClasspath(String classpath)
	{
		this.classpath = classpath;
	}

	@Override
	public InputStream get() throws Exception
	{
		return ClasspathReaderResourceFactory.class.getClassLoader().getResourceAsStream(this.classpath);
	}

	@Override
	public void release(InputStream resource) throws Exception
	{
		IOUtil.close(resource);
	}

	/**
	 * 构建{@linkplain ClasspathInputStreamResourceFactory}。
	 * 
	 * @param classpath
	 * @return
	 */
	public static ClasspathInputStreamResourceFactory valueOf(String classpath)
	{
		return new ClasspathInputStreamResourceFactory(classpath);
	}
}
