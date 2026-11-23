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

package org.datagear.analysis.support;

import java.io.Closeable;
import java.io.IOException;

import org.datagear.util.IOUtil;

/**
 * {@linkplain Closeable}持有类。
 * <p>
 * 调用{@linkplain #close()}将关闭{@linkplain #getCloseable()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class CloseableHolder<T extends Closeable> implements Closeable
{
	private final T closeable;

	public CloseableHolder(T closeable)
	{
		super();
		this.closeable = closeable;
	}

	public T getCloseable()
	{
		return closeable;
	}

	@Override
	public void close() throws IOException
	{
		IOUtil.close(this.closeable);
	}
}
