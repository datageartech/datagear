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
