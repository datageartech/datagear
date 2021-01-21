/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.persistence.support;

import org.datagear.persistence.PersistenceException;

/**
 * 找不到能确定唯一记录的列异常。
 * 
 * @author datagear@163.com
 *
 */
public class NonUniqueRecordColumnException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public NonUniqueRecordColumnException()
	{
		super();
	}

	public NonUniqueRecordColumnException(String message)
	{
		super(message);
	}

	public NonUniqueRecordColumnException(Throwable cause)
	{
		super(cause);
	}

	public NonUniqueRecordColumnException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
