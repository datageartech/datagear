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
		return new TextDataExportContext(dataExchange.getConnectionFactory(),
				new DataFormatContext(dataExchange.getDataFormat()));
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

	/**
	 * 文本导出上下文。
	 * 
	 * @author datagear@163.com
	 *
	 */
	protected static class TextDataExportContext extends DataExchangeContext
	{
		private DataFormatContext dataFormatContext;

		private DataIndex dataIndex;

		public TextDataExportContext()
		{
			super();
		}

		public TextDataExportContext(ConnectionFactory connectionFactory, DataFormatContext dataFormatContext)
		{
			super(connectionFactory);
			this.dataFormatContext = dataFormatContext;
		}

		public DataFormatContext getDataFormatContext()
		{
			return dataFormatContext;
		}

		public void setDataFormatContext(DataFormatContext dataFormatContext)
		{
			this.dataFormatContext = dataFormatContext;
		}

		public DataIndex getDataIndex()
		{
			return dataIndex;
		}

		public void setDataIndex(DataIndex dataIndex)
		{
			this.dataIndex = dataIndex;
		}
	}
}
