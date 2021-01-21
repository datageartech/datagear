/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.dataexchange.support;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import org.datagear.dataexchange.DataExchangeException;

/**
 * 导入JSON数据格式非法异常，不符合{@linkplain JsonDataFormat}的格式规范。
 * 
 * @author datagear@163.com
 *
 */
public class IllegalJsonDataFormatException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	public IllegalJsonDataFormatException()
	{
	}

	public IllegalJsonDataFormatException(JsonLocation jsonLocation, boolean mustbe, JsonParser.Event... events)
	{
		super(buildMessage(jsonLocation, mustbe, events));
	}

	public IllegalJsonDataFormatException(String message)
	{
		super(message);
	}

	public IllegalJsonDataFormatException(Throwable cause)
	{
		super(cause);
	}

	public IllegalJsonDataFormatException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public static String buildMessage(JsonLocation jsonLocation, boolean mustbe, JsonParser.Event... events)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(
				"The index [" + jsonLocation.getLineNumber() + ", " + jsonLocation.getColumnNumber() + "] JSON token");

		if (events == null || events.length == 0)
			sb.append(" is illegal.");
		else
		{
			if (mustbe)
				sb.append(" must be ");
			else
				sb.append(" must not be ");

			for (int i = 0; i < events.length; i++)
			{
				if (i != 0)
				{
					if (mustbe)
						sb.append(" or ");
					else
						sb.append(" nor ");
				}

				sb.append(events[i].toString());
			}
		}

		return sb.toString();
	}
}
