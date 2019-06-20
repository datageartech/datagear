/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.TextDataImportListener;

/**
 * 基于Cometd的子数据导入{@linkplain TextDataImportListener}。
 * 
 * @author datagear@163.com
 *
 */
public class CometdSubTextDataImportListener extends CometdSubDataExchangeListener implements TextDataImportListener
{
	private AtomicInteger _successCount = new AtomicInteger(0);
	private AtomicInteger _failCount = new AtomicInteger(0);

	public CometdSubTextDataImportListener()
	{
		super();
	}

	public CometdSubTextDataImportListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, String subDataExchangeId)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel, subDataExchangeId);
	}

	@Override
	public void onSuccess(int dataIndex)
	{
		_successCount.incrementAndGet();
	}

	@Override
	public void onFail(int dataIndex, DataExchangeException e)
	{
		_failCount.incrementAndGet();
	}

	@Override
	public void onSetNullColumnValue(int dataIndex, String columnName, String rawColumnValue, DataExchangeException e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected DataExchangeMessage buildFinishMessage(long duration)
	{
		return new TextImportSubFinish(this.getSubDataExchangeId(), duration, this._successCount.intValue(),
				this._failCount.intValue());
	}

	/**
	 * 子文本导入完成。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class TextImportSubFinish extends SubFinish
	{
		private int successCount;
		private int failCount;

		public TextImportSubFinish()
		{
			super();
		}

		public TextImportSubFinish(String subDataExchangeId, long duration, int successCount, int failCount)
		{
			super(subDataExchangeId, duration);
			this.successCount = successCount;
			this.failCount = failCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getFailCount()
		{
			return failCount;
		}

		public void setFailCount(int failCount)
		{
			this.failCount = failCount;
		}
	}
}
