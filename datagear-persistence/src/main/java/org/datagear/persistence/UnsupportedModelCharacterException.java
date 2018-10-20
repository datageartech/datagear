/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence;

import org.datagear.model.Model;

/**
 * 不支持的{@linkplain Model}特征异常。
 * <p>
 * 某些{@linkplain Model}特征在持久化时无法实现，此时将抛出此异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class UnsupportedModelCharacterException extends PersistenceException
{
	private static final long serialVersionUID = 1L;

	public UnsupportedModelCharacterException()
	{
		super();
	}

	public UnsupportedModelCharacterException(String message)
	{
		super(message);
	}

	public UnsupportedModelCharacterException(Throwable cause)
	{
		super(cause);
	}

	public UnsupportedModelCharacterException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
