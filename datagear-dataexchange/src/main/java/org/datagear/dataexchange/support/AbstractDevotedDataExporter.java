/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.datagear.dataexchange.DataExport;
import org.datagear.dataexchange.DataExportException;
import org.datagear.dataexchange.DevotedDataExporter;
import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象{@linkplain DevotedDataExporter}。
 * <p>
 * 它默认实现了{@linkplain #supports(DataExport)}，并且始终返回{@code true}。
 * </p>
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedDataExporter<T extends DataExport> extends DataExchangerSupport
		implements DevotedDataExporter<T>
{
	public AbstractDevotedDataExporter()
	{
		super();
	}

	@Override
	public boolean supports(T expt)
	{
		return true;
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @param databaseInfoResolver
	 * @return
	 * @throws DataExportException
	 */
	protected ColumnInfo[] getColumnInfos(Connection cn, ResultSet rs, DatabaseInfoResolver databaseInfoResolver)
			throws DataExportException
	{
		try
		{
			ResultSetMetaData resultSetMetaData = rs.getMetaData();

			return databaseInfoResolver.getColumnInfos(cn, resultSetMetaData);
		}
		catch (SQLException e)
		{
			throw new DataExportException(e);
		}
	}
}
