/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.datagear.util.resource.ConnectionFactory;
import org.datagear.util.resource.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据交换操作上下文。
 * 
 * @author datagear@163.com
 *
 */
public class DataExchangeContext
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DataExchangeContext.class);

	private ConnectionFactory connectionFactory;

	private Connection connection;

	private List<Closeable> contextCloseables = new ArrayList<Closeable>(3);

	private List<Closeable> localCloseables = new ArrayList<Closeable>(3);

	public DataExchangeContext()
	{
		super();
	}

	public DataExchangeContext(ConnectionFactory connectionFactory)
	{
		super();
		this.connectionFactory = connectionFactory;
	}

	public ConnectionFactory getConnectionFactory()
	{
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}

	/**
	 * 获取上下文{@linkplain Connection}。
	 * 
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception
	{
		if (this.connection == null)
			this.connection = this.connectionFactory.get();

		return this.connection;
	}

	/**
	 * 添加上下文待关闭{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	public void addContextCloseable(Closeable closeable)
	{
		this.contextCloseables.add(closeable);
	}

	/**
	 * 添加上下文待关闭资源。
	 * 
	 * @param <T>
	 * @param resourceFactory
	 * @param resource
	 */
	public <T> void addContextCloseable(ResourceFactory<T> resourceFactory, T resource)
	{
		this.contextCloseables.add(new ResourceFactoryCloseable(resourceFactory, resource));
	}

	/**
	 * 添加局部待关闭的{@linkplain Closeable}。
	 * 
	 * @param closeable
	 */
	public void addLocalClose(Closeable closeable)
	{
		this.localCloseables.add(closeable);
	}

	/**
	 * 添加局部待关闭资源。
	 * 
	 * @param <T>
	 * @param resourceFactory
	 * @param resource
	 */
	public <T> void addLocalCloseable(ResourceFactory<T> resourceFactory, T resource)
	{
		this.localCloseables.add(new ResourceFactoryCloseable(resourceFactory, resource));
	}

	/**
	 * 关闭上下文{@linkplain Connection}。
	 * 
	 * @return
	 */
	public boolean closeConnection()
	{
		if (this.connection == null)
			return false;

		try
		{
			this.connectionFactory.release(this.connection);
		}
		catch (Throwable e)
		{
			LOGGER.error("Release connection error", e);
		}

		return true;
	}

	/**
	 * 关闭所有上下文{@linkplain Closeable}。
	 */
	public void closeContextCloseables()
	{
		close(this.contextCloseables, false);
	}

	/**
	 * 关闭并清除所有上下文{@linkplain Closeable}。
	 */
	public void releaseContextCloseables()
	{
		close(this.contextCloseables, true);
	}

	/**
	 * 关闭所有局部{@linkplain Closeable}。
	 */
	public void closeLocalCloseables()
	{
		close(this.localCloseables, false);
	}

	/**
	 * 关闭并清除所有局部{@linkplain Closeable}。
	 */
	public void releaseLocalCloseables()
	{
		close(this.localCloseables, true);
	}

	/**
	 * 关闭所有{@linkplain Closeable}。
	 * 
	 * @param closeables
	 * @param clear
	 */
	protected void close(List<Closeable> closeables, boolean clear)
	{
		for (Closeable closeable : closeables)
		{
			try
			{
				closeable.close();
			}
			catch (Exception e)
			{
				LOGGER.error("Close resource error", e);
			}
		}

		if (clear)
			closeables.clear();
	}

	protected static class ResourceFactoryCloseable implements Closeable
	{
		private ResourceFactory<Object> resourceFactory;
		private Object resource;

		public ResourceFactoryCloseable()
		{
			super();
		}

		@SuppressWarnings("unchecked")
		public ResourceFactoryCloseable(ResourceFactory<?> resourceFactory, Object resource)
		{
			super();
			this.resourceFactory = (ResourceFactory<Object>) resourceFactory;
			this.resource = resource;
		}

		public ResourceFactory<Object> getResourceFactory()
		{
			return resourceFactory;
		}

		public void setResourceFactory(ResourceFactory<Object> resourceFactory)
		{
			this.resourceFactory = resourceFactory;
		}

		public Object getResource()
		{
			return resource;
		}

		public void setResource(Object resource)
		{
			this.resource = resource;
		}

		@Override
		public void close() throws IOException
		{
			try
			{
				this.resourceFactory.release(this.resource);
			}
			catch (Exception e)
			{
				LOGGER.error("Close resource error", e);
			}
		}
	}
}
