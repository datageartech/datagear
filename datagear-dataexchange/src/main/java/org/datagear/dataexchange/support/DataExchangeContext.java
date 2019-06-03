/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据交换操作上下文。
 * 
 * @author datagear@163.com
 *
 */
public class DataExchangeContext
{
	private List<Closeable> closeResources = new LinkedList<Closeable>();

	public DataExchangeContext()
	{
		super();
	}

	/**
	 * 添加一个待关闭的{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	public void addCloseResource(Closeable closeable)
	{
		this.closeResources.add(closeable);
	}

	/**
	 * 清除并关闭所有{@linkplain Closeable}。
	 * 
	 * @return
	 */
	public int clearCloseResources()
	{
		int size = closeResources.size();

		for (int i = 0; i < size; i++)
		{
			Closeable closeable = this.closeResources.get(i);

			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
			}
		}

		return size;
	}
}
