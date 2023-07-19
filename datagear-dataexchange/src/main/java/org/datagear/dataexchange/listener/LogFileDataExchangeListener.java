/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.dataexchange.listener;

import java.io.BufferedWriter;
import java.io.File;

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeListener;
import org.datagear.dataexchange.DataIndex;
import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 写入日志文件的{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public class LogFileDataExchangeListener implements DataExchangeListener
{
	private static final Logger LOGGER = LoggerFactory.getLogger(LogFileDataExchangeListener.class);

	private File logFile = null;

	private String logFileEncoding = IOUtil.CHARSET_UTF_8;

	private volatile BufferedWriter _logFileWriter = null;

	public LogFileDataExchangeListener()
	{
		super();
	}

	public boolean hasLogFile()
	{
		return (this.logFile != null);
	}

	public File getLogFile()
	{
		return logFile;
	}

	public void setLogFile(File logFile)
	{
		this.logFile = logFile;
	}

	public String getLogFileEncoding()
	{
		return logFileEncoding;
	}

	public void setLogFileEncoding(String logFileEncoding)
	{
		this.logFileEncoding = logFileEncoding;
	}

	@Override
	public void onStart()
	{
		if (hasLogFile())
		{
			try
			{
				this._logFileWriter = IOUtil.getWriter(this.logFile, this.logFileEncoding);
			}
			catch (Throwable t)
			{
				LOGGER.error("create log writer error", t);
			}

			writeStartLog();
		}
	}

	@Override
	public void onException(DataExchangeException e)
	{
		if (hasLogFile())
		{
			writeExceptionLog(e);
		}
	}

	@Override
	public void onSuccess()
	{
		if (hasLogFile())
		{
			writeSuccessLog();
		}
	}

	@Override
	public void onFinish()
	{
		if (hasLogFile())
		{
			writeFinishLog();
			IOUtil.close(this._logFileWriter);
		}
	}

	/**
	 * 写开始日志。
	 * 
	 * @param log
	 */
	protected void writeStartLog()
	{
		writeLogLine(getStartLog());
	}

	/**
	 * 写成功日志。
	 * 
	 * @param log
	 */
	protected void writeSuccessLog()
	{
		writeLogLine(getSuccessLog());
	}

	/**
	 * 写结束日志。
	 * 
	 * @param log
	 */
	protected void writeFinishLog()
	{
		writeLogLine(getFinishLog());
	}

	/**
	 * 写异常日志。
	 * 
	 * @param e
	 */
	protected void writeExceptionLog(DataExchangeException e)
	{
		writeLogLine(getExceptionLog(e));
	}

	/**
	 * 获取开始日志。
	 * 
	 * @return
	 */
	protected String getStartLog()
	{
		return "Start";
	}

	/**
	 * 获取成功日志。
	 * 
	 * @return
	 */
	protected String getSuccessLog()
	{
		return "Success";
	}

	/**
	 * 获取结束日志。
	 * 
	 * @return
	 */
	protected String getFinishLog()
	{
		return "Finish";
	}

	/**
	 * 获取异常日志。
	 * 
	 * @param e
	 * @return
	 */
	protected String getExceptionLog(DataExchangeException e)
	{
		return e.getMessage();
	}

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	protected void writeDataLog(DataIndex dataIndex, String log)
	{
		writeLogLine("[" + dataIndex + "] " + log);
	}

	/**
	 * 写一行日志。
	 * 
	 * @param log
	 * @return
	 */
	protected boolean writeLogLine(String log)
	{
		if (this._logFileWriter == null)
			return false;

		try
		{
			this._logFileWriter.write(log);
			this._logFileWriter.newLine();

			return true;
		}
		catch (Throwable t)
		{
			LOGGER.error("write log error", t);
			return false;
		}
	}
}
