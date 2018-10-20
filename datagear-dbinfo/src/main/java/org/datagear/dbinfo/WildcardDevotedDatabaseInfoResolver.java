/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.Connection;

/**
 * 通配{@linkplain DevotedDatabaseInfoResolver}。
 * <p>
 * 此类的{@linkplain #supports(Connection)}方法始终返回{@linkplain #isSupported()}的值（默认为{@code true}）。
 * </p>
 * <p>
 * 此类可以作为{@linkplain GenericDatabaseInfoResolver#getDevotedDatabaseInfoResolvers()}的最后一个元素，用于获取通配数据库信息，同时避免它抛出{@linkplain UnsupportedDatabaseInfoResolverException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class WildcardDevotedDatabaseInfoResolver extends AbstractDevotedDatabaseInfoResolver
{
	private boolean supported = true;

	public WildcardDevotedDatabaseInfoResolver()
	{
		super();
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
