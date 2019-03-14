/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.sqlpad;

/**
 * SQL执行{@linkplain Runnable}。
 * 
 * @author datagear@163.com
 *
 */
public class SqlpadExecutionRunnable implements Runnable
{
	private SqlpadCometdService sqlpadCometdService;

	private String sqlpadChannelId;

	public SqlpadExecutionRunnable()
	{
		super();
	}

	public SqlpadExecutionRunnable(SqlpadCometdService sqlpadCometdService, String sqlpadChannelId)
	{
		super();
		this.sqlpadCometdService = sqlpadCometdService;
		this.sqlpadChannelId = sqlpadChannelId;
	}

	public SqlpadCometdService getSqlpadCometdService()
	{
		return sqlpadCometdService;
	}

	public void setSqlpadCometdService(SqlpadCometdService sqlpadCometdService)
	{
		this.sqlpadCometdService = sqlpadCometdService;
	}

	public String getSqlpadChannelId()
	{
		return sqlpadChannelId;
	}

	public void setSqlpadChannelId(String sqlpadChannelId)
	{
		this.sqlpadChannelId = sqlpadChannelId;
	}

	@Override
	public void run()
	{
		for (int i = 0; i < 100; i++)
		{
			this.sqlpadCometdService.publish(this.sqlpadChannelId, this.sqlpadChannelId + ": sql [" + i + "] executed");

			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
}
