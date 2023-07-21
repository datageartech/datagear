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

import org.datagear.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志文件支持类。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractLogFileSupport
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLogFileSupport.class);

	private File logFile = null;

	private String logFileEncoding = IOUtil.CHARSET_UTF_8;

	private volatile BufferedWriter _logFileWriter = null;

	public AbstractLogFileSupport()
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

	/**
	 * 准备日志资源。
	 */
	protected boolean prepareLogResource()
	{
		if (!hasLogFile())
			return false;

		try
		{
			this._logFileWriter = IOUtil.getWriter(this.logFile, this.logFileEncoding);
			return true;
		}
		catch (Throwable t)
		{
			LOGGER.error("create log writer error", t);
			return false;
		}
	}

	/**
	 * 释放日志资源。
	 */
	protected void releaseLogResource()
	{
		IOUtil.close(this._logFileWriter);
	}

	/**
	 * 写一行日志。
	 * 
	 * @param log
	 * @return
	 */
	protected boolean writeLogLine(String log)
	{
		if (!hasLogFile())
			return false;

		try
		{
			// 这里不使用BufferedWriter.newLine()，因为在多线程写时，可能会错行
			this._logFileWriter.write(log + IOUtil.LINE_SEPARATOR);
			return true;
		}
		catch (Throwable t)
		{
			LOGGER.error("write log error", t);
			return false;
		}
	}
}
