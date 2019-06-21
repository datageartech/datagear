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
	private AtomicInteger _ignoreCount = new AtomicInteger(0);

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
	public void onIgnore(int dataIndex, DataExchangeException e)
	{
		_ignoreCount.incrementAndGet();
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
				this._ignoreCount.intValue());
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
		private int ignoreCount;

		public TextImportSubFinish()
		{
			super();
		}

		public TextImportSubFinish(String subDataExchangeId, long duration, int successCount, int ignoreCount)
		{
			super(subDataExchangeId, duration);
			this.successCount = successCount;
			this.ignoreCount = ignoreCount;
		}

		public int getSuccessCount()
		{
			return successCount;
		}

		public void setSuccessCount(int successCount)
		{
			this.successCount = successCount;
		}

		public int getIgnoreCount()
		{
			return ignoreCount;
		}

		public void setIgnoreCount(int ignoreCount)
		{
			this.ignoreCount = ignoreCount;
		}
	}
}
