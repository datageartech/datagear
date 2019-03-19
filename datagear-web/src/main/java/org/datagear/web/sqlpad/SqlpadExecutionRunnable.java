/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.sqlpad;

import java.util.List;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.web.util.SqlScriptParser.SqlStatement;

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

	private List<SqlStatement> sqlStatements;

	private ServerChannel _sqlpadServerChannel;

	public SqlpadExecutionRunnable()
	{
		super();
	}

	public SqlpadExecutionRunnable(SqlpadCometdService sqlpadCometdService, String sqlpadChannelId,
			List<SqlStatement> sqlStatements)
	{
		super();
		this.sqlpadCometdService = sqlpadCometdService;
		this.sqlpadChannelId = sqlpadChannelId;
		this.sqlStatements = sqlStatements;
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

	public List<SqlStatement> getSqlStatements()
	{
		return sqlStatements;
	}

	public void setSqlStatements(List<SqlStatement> sqlStatements)
	{
		this.sqlStatements = sqlStatements;
	}

	/**
	 * 初始化。
	 * <p>
	 * 此方法应该在{@linkplain #run()}之前调用。
	 * </p>
	 */
	public void init()
	{
		this._sqlpadServerChannel = this.sqlpadCometdService.getChannelWithCreation(this.sqlpadChannelId);
	}

	@Override
	public void run()
	{
		this.sqlpadCometdService.sendStartMessage(this._sqlpadServerChannel);

		try
		{
			for (int i = 0, len = sqlStatements.size(); i < len; i++)
			{
				SqlStatement sqlStatement = sqlStatements.get(i);

				// TODO 执行SQL

				this.sqlpadCometdService.sendSuccessMessage(_sqlpadServerChannel, sqlStatement, i);

				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					break;
				}
			}
		}
		finally
		{
			this.sqlpadCometdService.sendFinishMessage(this._sqlpadServerChannel);
		}
	}
}
