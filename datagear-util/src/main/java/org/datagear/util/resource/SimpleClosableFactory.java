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

package org.datagear.util.resource;

import java.io.Closeable;

/**
 * 简单{@linkplain ResourceFactory}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleClosableFactory<T extends Closeable> implements ResourceFactory<T>
{
	private T resource;

	private boolean closeOnRelease = true;

	public SimpleClosableFactory()
	{
		super();
	}

	public SimpleClosableFactory(T resource, boolean closeOnRelease)
	{
		super();
		this.resource = resource;
		this.closeOnRelease = closeOnRelease;
	}

	public T getResource() throws Exception
	{
		return resource;
	}

	public void setResource(T resource)
	{
		this.resource = resource;
	}

	public boolean isCloseOnRelease()
	{
		return closeOnRelease;
	}

	public void setCloseOnRelease(boolean closeOnRelease)
	{
		this.closeOnRelease = closeOnRelease;
	}

	@Override
	public T get() throws Exception
	{
		return this.resource;
	}

	@Override
	public void release(T resource) throws Exception
	{
		if (this.resource != resource)
			throw new IllegalStateException();

		if (this.closeOnRelease && this.resource != null)
			this.resource.close();
	}

	/**
	 * 构建{@linkplain SimpleClosableFactory}。
	 * 
	 * @param <T>
	 * @param resource
	 * @return
	 */
	public static <T extends Closeable> SimpleClosableFactory<T> valueOf(T resource)
	{
		return new SimpleClosableFactory<T>(resource, true);
	}

	/**
	 * 构建{@linkplain SimpleClosableFactory}。
	 * 
	 * @param <T>
	 * @param resource
	 * @param closeOnRelease
	 * @return
	 */
	public static <T extends Closeable> SimpleClosableFactory<T> valueOf(T resource, boolean closeOnRelease)
	{
		return new SimpleClosableFactory<T>(resource, closeOnRelease);
	}
}
