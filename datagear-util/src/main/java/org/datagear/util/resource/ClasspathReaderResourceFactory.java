/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.util.resource;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 类路径字符输入流{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class ClasspathReaderResourceFactory extends AbstractReaderResourceFactory
{
	private String classpath;

	public ClasspathReaderResourceFactory()
	{
		super();
	}

	public ClasspathReaderResourceFactory(String classpath)
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
	protected InputStream getInputStream() throws Exception
	{
		return ClasspathReaderResourceFactory.class.getClassLoader().getResourceAsStream(this.classpath);
	}

	/**
	 * 构建{@linkplain ClasspathReaderResourceFactory}。
	 * 
	 * @param classpath
	 * @return
	 */
	public static ClasspathReaderResourceFactory valueOf(String classpath)
	{
		return new ClasspathReaderResourceFactory(classpath);
	}

	/**
	 * 构建{@linkplain ClasspathReaderResourceFactory}。
	 * 
	 * @param classpath
	 * @param charset
	 * @return
	 */
	public static ClasspathReaderResourceFactory valueOf(String classpath, Charset charset)
	{
		ClasspathReaderResourceFactory resourceFactory = new ClasspathReaderResourceFactory(classpath);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}

	/**
	 * 构建{@linkplain ClasspathReaderResourceFactory}。
	 * 
	 * @param classpath
	 * @param charsetName
	 * @return
	 */
	public static ClasspathReaderResourceFactory valueOf(String classpath, String charsetName)
	{
		Charset charset = Charset.forName(charsetName);

		ClasspathReaderResourceFactory resourceFactory = new ClasspathReaderResourceFactory(classpath);
		resourceFactory.setCharset(charset);

		return resourceFactory;
	}
}
