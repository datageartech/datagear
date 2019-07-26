/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.datagear.dbinfo.ColumnInfo;
import org.datagear.dbinfo.DatabaseInfoResolver;

/**
 * 抽象文本导出服务。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class AbstractDevotedTextDataExportService<T extends TextDataExport>
		extends AbstractDevotedDataExchangeService<T>
{
	private DatabaseInfoResolver databaseInfoResolver;

	public AbstractDevotedTextDataExportService()
	{
		super();
	}

	public AbstractDevotedTextDataExportService(DatabaseInfoResolver databaseInfoResolver)
	{
		super();
		this.databaseInfoResolver = databaseInfoResolver;
	}

	public DatabaseInfoResolver getDatabaseInfoResolver()
	{
		return databaseInfoResolver;
	}

	public void setDatabaseInfoResolver(DatabaseInfoResolver databaseInfoResolver)
	{
		this.databaseInfoResolver = databaseInfoResolver;
	}

	@Override
	protected DataExchangeContext createDataExchangeContext(T dataExchange)
	{
		return new IndexFormatDataExchangeContext(dataExchange.getConnectionFactory(),
				new DataFormatContext(dataExchange.getDataFormat()));
	}

	/**
	 * 转换{@linkplain DataExchangeContext}类型。
	 * 
	 * @param context
	 * @return
	 */
	protected IndexFormatDataExchangeContext castDataExchangeContext(DataExchangeContext context)
	{
		return (IndexFormatDataExchangeContext) context;
	}

	/**
	 * 获取{@linkplain ResultSet}列信息。
	 * 
	 * @param cn
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<ColumnInfo> getColumnInfos(Connection cn, ResultSet rs) throws SQLException
	{
		return super.getColumnInfos(cn, rs, this.databaseInfoResolver);
	}
}
