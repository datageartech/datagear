/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import java.sql.Connection;

import org.datagear.meta.Column;
import org.datagear.meta.Table;
import org.datagear.persistence.Dialect;
import org.datagear.persistence.PstParamMapper;
import org.datagear.persistence.PstParamMapperException;

/**
 * 简单{@linkplain PstParamMapper}。
 * <p>
 * 它什么也不做，直接返回原值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SimplePstParamMapper implements PstParamMapper
{
	public SimplePstParamMapper()
	{
		super();
	}

	@Override
	public Object map(Connection cn, Dialect dialect, Table table, Column column, Object value)
			throws PstParamMapperException
	{
		return value;
	}
}
