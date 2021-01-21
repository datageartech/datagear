/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.persistence.support;

import org.datagear.persistence.SqlParamValueMapper;

/**
 * 抽象{@linkplain SqlParamValueMapper}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractSqlParamValueMapper extends PersistenceSupport implements SqlParamValueMapper
{
	public AbstractSqlParamValueMapper()
	{
		super();
	}
}
