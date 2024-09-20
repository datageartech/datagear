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

package org.datagear.util.function;

import java.util.function.Supplier;

/**
 * 仅执行{@linkplain #getSupplier()}一次{@linkplain Supplier#get()}的{@linkplain Supplier}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public class OnceSupplier<T> implements Supplier<T>
{
	private final Supplier<T> supplier;

	private boolean supplied = false;
	private T result = null;

	public OnceSupplier()
	{
		super();
		this.supplier = null;
	}

	public OnceSupplier(Supplier<T> supplier)
	{
		super();
		this.supplier = supplier;
	}

	public Supplier<T> getSupplier()
	{
		return supplier;
	}

	public boolean isSupplied()
	{
		return supplied;
	}

	protected void setSupplied(boolean supplied)
	{
		this.supplied = supplied;
	}

	public T getResult()
	{
		return result;
	}

	protected void setResult(T result)
	{
		this.result = result;
	}

	@Override
	public T get()
	{
		if (this.supplier == null)
			return null;

		if (!this.supplied)
		{
			this.result = this.supplier.get();
			this.supplied = true;
		}

		return this.result;
	}
}
