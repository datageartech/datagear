/*
 * Copyright (c) 2018 by datagear.org.
 */

package org.datagear.dbmodel;

import java.sql.Connection;

import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 通配{@linkplain DevotedDatabaseModelResolver}。
 * <p>
 * 此类的{@linkplain #supports(Connection)}方法始终返回{@linkplain #isSupported()}的值（默认为{@code true}）。
 * </p>
 * <p>
 * 此类可以作为{@linkplain GenericDatabaseModelResolver#getDevotedDatabaseModelResolvers()}的最后一个元素，用于解析通配模型信息，同时避免它抛出{@linkplain UnsupportedDatabaseModelResolverException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class WildcardDevotedDatabaseModelResolver extends AbstractDevotedDatabaseModelResolver
{
	private boolean supported = true;

	public WildcardDevotedDatabaseModelResolver()
	{
		super();
	}

	public WildcardDevotedDatabaseModelResolver(DatabaseInfoResolver databaseInfoResolver,
			PrimitiveModelResolver primitiveModelResolver)
	{
		super(databaseInfoResolver, primitiveModelResolver);
	}

	public boolean isSupported()
	{
		return supported;
	}

	public void setSupported(boolean supported)
	{
		this.supported = supported;
	}

	@Override
	public boolean supports(Connection cn)
	{
		return this.supported;
	}
}
