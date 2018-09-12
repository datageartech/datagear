/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.persistence.columnconverter;

import org.datagear.persistence.features.ColumnConverter;
import org.datagear.persistence.support.UUID;

/**
 * 抽象{@linkplain ColumnConverter}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractColumnConverter implements ColumnConverter
{
	public AbstractColumnConverter()
	{
		super();
	}

	/**
	 * 生成一个UUID字符串。
	 * 
	 * @return
	 */
	protected String uuid()
	{
		return UUID.gen();
	}
}
