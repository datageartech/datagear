/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PstValueMapper;
import org.datagear.persistence.PstValueMapperException;

/**
 * 简单{@linkplain PstValueMapper}。
 * 
 * @author datagear@163.com
 *
 */
public class SimplePstValueMapper implements PstValueMapper
{
	public SimplePstValueMapper()
	{
		super();
	}

	@Override
	public Object map(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstValueMapperException
	{
		return value;
	}
}
