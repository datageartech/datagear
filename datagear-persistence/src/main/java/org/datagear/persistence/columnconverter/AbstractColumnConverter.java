/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.columnconverter;

import org.datagear.persistence.features.ColumnConverter;
import org.datagear.util.IDUtil;

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
		return IDUtil.uuid();
	}
}
