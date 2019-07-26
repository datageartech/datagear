/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.dataexchange;

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
}
