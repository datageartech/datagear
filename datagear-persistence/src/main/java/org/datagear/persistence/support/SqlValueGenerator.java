/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.persistence.support;

import org.datagear.model.Model;
import org.datagear.model.Property;
import org.datagear.persistence.features.ValueGenerator;

/**
 * SQL语句属性值生成器。
 * <p>
 * 此类用于支持插入数据库序列属性值。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class SqlValueGenerator implements ValueGenerator
{
	private String sql;

	public SqlValueGenerator()
	{
		super();
	}

	public SqlValueGenerator(String sql)
	{
		super();
		this.sql = sql;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	@Override
	public boolean isSql(Model model, Property property, Object obj)
	{
		return true;
	}

	@Override
	public Object generate(Model model, Property property, Object obj)
	{
		return this.sql;
	}
}