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

package org.datagear.util;

import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * 解压zip时的{@code MALFORMED}异常。
 * <p>
 * 当解压zip指定的字符集不匹配时（比如zip里包含{@code GBK}中文名但是以{@code UTF-8}解压），
 * {@linkplain ZipInputStream}会抛出{@linkplain IllegalArgumentException IllegalArgumentException("MALFORMED")}异常，
 * 此异常即是用户标识这种情况。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class MalformedZipException extends IOException
{
	private static final long serialVersionUID = 1L;

	public MalformedZipException()
	{
		super();
	}

	public MalformedZipException(String message)
	{
		super(message);
	}

	public MalformedZipException(Throwable cause)
	{
		super(cause);
	}
	
	public MalformedZipException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public static boolean isMalformedZipException(IllegalArgumentException e)
	{
		String msg = e.getMessage();
		return (msg != null && msg.toUpperCase().indexOf("MALFORMED") >= 0);
	}
}
