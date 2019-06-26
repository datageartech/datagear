/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.web.cometd.dataexchange;

import java.util.Locale;

import org.cometd.bayeux.server.ServerChannel;
import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeListener;
import org.datagear.dataexchange.ExceptionResolve;
import org.springframework.context.MessageSource;

/**
 * 基于Cometd的子数据交换{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class CometdSubDataExchangeListener extends CometdDataExchangeListener
{
	private String subDataExchangeId;

	public CometdSubDataExchangeListener()
	{
		super();
	}

	public CometdSubDataExchangeListener(DataExchangeCometdService dataExchangeCometdService,
			ServerChannel dataExchangeServerChannel, MessageSource messageSource, Locale locale,
			String subDataExchangeId)
	{
		super(dataExchangeCometdService, dataExchangeServerChannel, messageSource, locale);
		this.subDataExchangeId = subDataExchangeId;
	}

	public String getSubDataExchangeId()
	{
		return subDataExchangeId;
	}

	public void setSubDataExchangeId(String subDataExchangeId)
	{
		this.subDataExchangeId = subDataExchangeId;
	}

	@Override
	protected DataExchangeMessage buildStartMessage()
	{
		return new SubStart(this.subDataExchangeId);
	}

	@Override
	protected DataExchangeMessage buildExceptionMessage(DataExchangeException e)
	{
		return new SubException(this.subDataExchangeId, resolveDataExchangeExceptionI18n(e), evalDuration());
	}

	@Override
	protected DataExchangeMessage buildSuccessMessage()
	{
		return new SubSuccess(this.subDataExchangeId, evalDuration());
	}

	@Override
	protected DataExchangeMessage buildFinishMessage()
	{
		return new SubFinish(this.subDataExchangeId);
	}

	/**
	 * 子数据交换开始。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubStart extends SubDataExchangeMessage
	{
		public SubStart()
		{
			super();
		}

		public SubStart(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 子数据交换异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubException extends SubDataExchangeMessage
	{
		private String content;

		private long duration;

		public SubException()
		{
			super();
		}

		public SubException(String subDataExchangeId, String content, long duration)
		{
			super(subDataExchangeId);
			this.content = content;
			this.duration = duration;
		}

		public String getContent()
		{
			return content;
		}

		public void setContent(String content)
		{
			this.content = content;
		}

		public long getDuration()
		{
			return duration;
		}

		public void setDuration(long duration)
		{
			this.duration = duration;
		}
	}

	/**
	 * 子数据交换成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSuccess extends SubDataExchangeMessage
	{
		private long duration;

		public SubSuccess()
		{
			super();
		}

		public SubSuccess(String subDataExchangeId, long duration)
		{
			super(subDataExchangeId);
			this.duration = duration;
		}

		public long getDuration()
		{
			return duration;
		}

		public void setDuration(long duration)
		{
			this.duration = duration;
		}
	}

	/**
	 * 子数据交换完成。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubFinish extends SubDataExchangeMessage
	{
		public SubFinish()
		{
			super();
		}

		public SubFinish(String subDataExchangeId)
		{
			super(subDataExchangeId);
		}
	}

	/**
	 * 带有成功、失败数量统计的子数据交换进行中。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubExchangingWithCount extends SubDataExchangeMessage
	{
		private int successCount;

		private int failCount;

		public SubExchangingWithCount()
		{
			super();
		}

		public SubExchangingWithCount(String subDataExchangeId, int successCount, int failCount)
		{
			super(subDataExchangeId);
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

	/**
	 * 带有成功、失败数量统计的子数据交换异常。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubExceptionWithCount extends SubException
	{
		private ExceptionResolve exceptionResolve;

		private int successCount;

		private int failCount;

		public SubExceptionWithCount()
		{
			super();
		}

		public SubExceptionWithCount(String subDataExchangeId, String content, long duration,
				ExceptionResolve exceptionResolve, int successCount, int failCount)
		{
			super(subDataExchangeId, content, duration);
			this.exceptionResolve = exceptionResolve;
			this.successCount = successCount;
			this.failCount = failCount;
		}

		public ExceptionResolve getExceptionResolve()
		{
			return exceptionResolve;
		}

		public void setExceptionResolve(ExceptionResolve exceptionResolve)
		{
			this.exceptionResolve = exceptionResolve;
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

	/**
	 * 子文本导入成功。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class SubSuccessWithCount extends SubSuccess
	{
		private int successCount;

		private int failCount;

		private String ignoreException;

		public SubSuccessWithCount()
		{
			super();
		}

		public SubSuccessWithCount(String subDataExchangeId, long duration, int successCount, int failCount)
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

		public String getIgnoreException()
		{
			return ignoreException;
		}

		public void setIgnoreException(String ignoreException)
		{
			this.ignoreException = ignoreException;
		}
	}
}
