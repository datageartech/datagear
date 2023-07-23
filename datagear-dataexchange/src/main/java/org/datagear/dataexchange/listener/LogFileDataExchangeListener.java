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

import org.datagear.dataexchange.DataExchangeException;
import org.datagear.dataexchange.DataExchangeListener;
import org.datagear.dataexchange.DataIndex;

/**
 * 写入日志文件的{@linkplain DataExchangeListener}。
 * 
 * @author datagear@163.com
 *
 */
public class LogFileDataExchangeListener extends AbstractLogFileSupport implements DataExchangeListener
{
	public LogFileDataExchangeListener()
	{
		super();
	}

	@Override
	public void onStart()
	{
		if (prepareLogResource())
		{
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
			releaseLogResource();
		}
	}

	/**
	 * 写开始日志。
	 * 
	 * @param log
	 */
	public void writeStartLog()
	{
		writeLogLine(getStartLog());
	}

	/**
	 * 写成功日志。
	 * 
	 * @param log
	 */
	public void writeSuccessLog()
	{
		writeLogLine(getSuccessLog());
	}

	/**
	 * 写结束日志。
	 * 
	 * @param log
	 */
	public void writeFinishLog()
	{
		writeLogLine(getFinishLog());
	}

	/**
	 * 写异常日志。
	 * 
	 * @param e
	 */
	public void writeExceptionLog(DataExchangeException e)
	{
		writeLogLine(getExceptionLog(e));
	}

	/**
	 * 获取开始日志。
	 * 
	 * @return
	 */
	public String getStartLog()
	{
		return "Start";
	}

	/**
	 * 获取成功日志。
	 * 
	 * @return
	 */
	public String getSuccessLog()
	{
		return "Success";
	}

	/**
	 * 获取结束日志。
	 * 
	 * @return
	 */
	public String getFinishLog()
	{
		return "Finish";
	}

	/**
	 * 获取异常日志。
	 * 
	 * @param e
	 * @return
	 */
	public String getExceptionLog(DataExchangeException e)
	{
		return e.getMessage();
	}

	/**
	 * 写一条数据日志。
	 * 
	 * @param dataIndex
	 * @param log
	 */
	public void writeDataLog(DataIndex dataIndex, String log)
	{
		writeLogLine("[" + dataIndex + "] " + log);
	}
}
