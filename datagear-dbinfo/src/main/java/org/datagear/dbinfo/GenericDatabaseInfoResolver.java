/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dbinfo;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.datagear.connection.ConnectionOption;

/**
 * 通用数据库信息解析器。
 * <p>
 * 它从其包含的{@linkplain DevotedDatabaseInfoResolver}中（
 * {@linkplain #getDevotedDatabaseInfoResolvers()}，越靠前越优先使用）查找能够处理给定{@link Connection}
 * 的那一个，并使用其API。
 * </p>
 * <p>
 * 如果没有查找到能处理给定{@link Connection}的{@linkplain DevotedDatabaseInfoResolver}，此类将抛出
 * {@linkplain UnsupportedDatabaseInfoResolverException}异常。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class GenericDatabaseInfoResolver implements DatabaseInfoResolver
{
	private List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolvers = null;

	public GenericDatabaseInfoResolver()
	{
		super();
	}

	public GenericDatabaseInfoResolver(List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolvers)
	{
		super();
		this.devotedDatabaseInfoResolvers = devotedDatabaseInfoResolvers;
	}

	public List<DevotedDatabaseInfoResolver> getDevotedDatabaseInfoResolvers()
	{
		return devotedDatabaseInfoResolvers;
	}

	public void setDevotedDatabaseInfoResolvers(List<DevotedDatabaseInfoResolver> devotedDatabaseInfoResolvers)
	{
		this.devotedDatabaseInfoResolvers = devotedDatabaseInfoResolvers;
	}

	@Override
	public DatabaseInfo getDatabaseInfo(Connection cn) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getDatabaseInfo(cn);
	}

	@Override
	public TableInfo[] getTableInfos(Connection cn) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getTableInfos(cn);
	}

	@Override
	public TableInfo getRandomTableInfo(Connection cn) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getRandomTableInfo(cn);
	}

	@Override
	public TableInfo getTableInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getTableInfo(cn, tableName);
	}

	@Override
	public EntireTableInfo getEntireTableInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getEntireTableInfo(cn, tableName);
	}

	@Override
	public ColumnInfo[] getColumnInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getColumnInfos(cn, tableName);
	}

	@Override
	public ColumnInfo getRandomColumnInfo(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getRandomColumnInfo(cn, tableName);
	}

	@Override
	public ColumnInfo[] getColumnInfos(Connection cn, ResultSetMetaData resultSetMetaData)
			throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getColumnInfos(cn, resultSetMetaData);
	}

	@Override
	public String[] getPrimaryKeyColumnNames(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getPrimaryKeyColumnNames(cn, tableName);
	}

	@Override
	public String[][] getUniqueKeyColumnNames(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getUniqueKeyColumnNames(cn, tableName);
	}

	@Override
	public ImportedKeyInfo[] getImportedKeyInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getImportedKeyInfos(cn, tableName);
	}

	@Override
	public ExportedKeyInfo[] getExportedKeyInfos(Connection cn, String tableName) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getExportedKeyInfos(cn, tableName);
	}

	@Override
	public String[][] getImportedTables(Connection cn, String[] tables) throws DatabaseInfoResolverException
	{
		DatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolverNotNull(cn);

		return databaseInfoResolver.getImportedTables(cn, tables);
	}

	/**
	 * 获取支持指定{@code url}的{@linkplain DatabaseInfoResolver}。
	 * 
	 * @param cn
	 * @return
	 * @throws UnsupportedDatabaseInfoResolverException
	 */
	protected DevotedDatabaseInfoResolver doGetDevotedDatabaseInfoResolverNotNull(Connection cn)
			throws UnsupportedDatabaseInfoResolverException
	{
		DevotedDatabaseInfoResolver databaseInfoResolver = doGetDevotedDatabaseInfoResolver(cn);

		if (databaseInfoResolver == null)
			throw new UnsupportedDatabaseInfoResolverException(ConnectionOption.valueOf(cn));

		return databaseInfoResolver;
	}

	/**
	 * 获取支持指定{@code cn}的{@linkplain DevotedDatabaseInfoResolver}。
	 * <p>
	 * 如果没有，则返回{@code null}。
	 * </p>
	 * 
	 * @param cn
	 * @return
	 */
	protected DevotedDatabaseInfoResolver doGetDevotedDatabaseInfoResolver(Connection cn)
	{
		if (this.devotedDatabaseInfoResolvers == null)
			return null;

		for (DevotedDatabaseInfoResolver devotedDatabaseInfoResolver : this.devotedDatabaseInfoResolvers)
		{
			if (devotedDatabaseInfoResolver.supports(cn))
				return devotedDatabaseInfoResolver;
		}

		return null;
	}
}
