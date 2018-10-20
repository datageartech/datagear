/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbmodel;

import java.sql.Connection;
import java.util.List;

import org.datagear.connection.ConnectionOption;
import org.datagear.model.Model;
import org.datagear.model.ModelManager;

/**
 * 通用数据库{@linkplain Model}解析器。
 * <p>
 * 它从其包含的{@linkplain DevotedDatabaseModelResolver}中（
 * {@linkplain #getDatabaseModelResolvers()}，越靠前越优先使用）查找能够处理给定{@link Connection}
 * 的那一个，并使用其API。
 * </p>
 * <p>
 * 如果没有查找到能处理给定{@link Connection}的{@linkplain DevotedDatabaseModelResolver}
 * ，此类将抛出 {@linkplain UnsupportedDatabaseModelResolverException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class GenericDatabaseModelResolver implements DatabaseModelResolver
{
	private List<DevotedDatabaseModelResolver> devotedDatabaseModelResolvers = null;

	public GenericDatabaseModelResolver()
	{
		super();
	}

	public GenericDatabaseModelResolver(List<DevotedDatabaseModelResolver> devotedDatabaseModelResolvers)
	{
		super();
		this.devotedDatabaseModelResolvers = devotedDatabaseModelResolvers;
	}

	public List<DevotedDatabaseModelResolver> getDevotedDatabaseModelResolvers()
	{
		return devotedDatabaseModelResolvers;
	}

	public void setDevotedDatabaseModelResolvers(List<DevotedDatabaseModelResolver> devotedDatabaseModelResolvers)
	{
		this.devotedDatabaseModelResolvers = devotedDatabaseModelResolvers;
	}

	@Override
	public Model resolve(Connection cn, ModelManager globalModelManager, ModelManager localModelManager, String table)
			throws DatabaseModelResolverException
	{
		DatabaseModelResolver databaseModelResolver = doGetDevotedDatabaseModelResolverNotNull(cn);

		return databaseModelResolver.resolve(cn, globalModelManager, localModelManager, table);
	}

	/**
	 * 获取支持指定{@linkplain Connection}的{@linkplain DevotedDatabaseModelResolver}。
	 * 
	 * @param cn
	 * @return
	 * @throws UnsupportedDatabaseModelResolverException
	 */
	protected DevotedDatabaseModelResolver doGetDevotedDatabaseModelResolverNotNull(Connection cn)
			throws UnsupportedDatabaseModelResolverException
	{
		DevotedDatabaseModelResolver devotedDatabaseModelResolver = doGetDevotedDatabaseModelResolver(cn);

		if (devotedDatabaseModelResolver == null)
			throw new UnsupportedDatabaseModelResolverException(ConnectionOption.valueOf(cn));

		return devotedDatabaseModelResolver;
	}

	/**
	 * 获取支持指定{@linkplain Connection}的{@linkplain DevotedDatabaseModelResolver}。
	 * <p>
	 * 如果没有，则返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	protected DevotedDatabaseModelResolver doGetDevotedDatabaseModelResolver(Connection cn)
	{
		if (this.devotedDatabaseModelResolvers == null)
			return null;

		for (DevotedDatabaseModelResolver devotedDatabaseModelResolver : this.devotedDatabaseModelResolvers)
		{
			if (devotedDatabaseModelResolver.supports(cn))
				return devotedDatabaseModelResolver;
		}

		return null;
	}
}
