/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

import java.io.IOException;

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
}
