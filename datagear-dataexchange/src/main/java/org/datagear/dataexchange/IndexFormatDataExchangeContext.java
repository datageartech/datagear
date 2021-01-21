/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

import org.datagear.util.resource.ConnectionFactory;

/**
 * 带当前数据索引信息和格式化上下文的{@linkplain DataExchangeContext}。
 * 
 * @author datagear@163.com
 *
 */
public class IndexFormatDataExchangeContext extends DataExchangeContext
{
	private DataFormatContext dataFormatContext;

	private DataIndex dataIndex;

	public IndexFormatDataExchangeContext()
	{
		super();
	}

	public IndexFormatDataExchangeContext(ConnectionFactory connectionFactory, DataFormatContext dataFormatContext)
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

	/**
	 * 构建{@linkplain IndexFormatDataExchangeContext}。
	 * 
	 * @param dataExchange
	 * @return
	 */
	public static IndexFormatDataExchangeContext valueOf(FormatDataExchange dataExchange)
	{
		return new IndexFormatDataExchangeContext(dataExchange.getConnectionFactory(),
				new DataFormatContext(dataExchange.getDataFormat()));
	}

	/**
	 * 构建{@linkplain IndexFormatDataExchangeContext}。
	 * 
	 * @param connectionFactory
	 * @param dataFormat
	 * @return
	 */
	public static IndexFormatDataExchangeContext valueOf(ConnectionFactory connectionFactory, DataFormat dataFormat)
	{
		return new IndexFormatDataExchangeContext(connectionFactory, new DataFormatContext(dataFormat));
	}

	/**
	 * 构建{@linkplain IndexFormatDataExchangeContext}。
	 * 
	 * @param connectionFactory
	 * @param dataFormatContext
	 * @return
	 */
	public static IndexFormatDataExchangeContext valueOf(ConnectionFactory connectionFactory,
			DataFormatContext dataFormatContext)
	{
		return new IndexFormatDataExchangeContext(connectionFactory, dataFormatContext);
	}

	/**
	 * 将{@linkplain DataExchangeContext}转换为{@linkplain IndexFormatDataExchangeContext}。
	 * 
	 * @param context
	 * @return
	 */
	public static IndexFormatDataExchangeContext cast(DataExchangeContext context)
	{
		return (IndexFormatDataExchangeContext) context;
	}
}
