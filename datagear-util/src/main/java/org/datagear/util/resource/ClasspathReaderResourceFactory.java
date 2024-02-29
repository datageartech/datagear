/*
 * Copyright 2018-2024 datagear.tech
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
