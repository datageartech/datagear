/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 指定索引的数据交换异常。
 * 
 * @author datagear@163.com
 *
 */
public class IndexDataExchangeException extends DataExchangeException
{
	private static final long serialVersionUID = 1L;

	private DataIndex dataIndex;

	public IndexDataExchangeException(DataIndex dataIndex)
	{
		super();
		this.dataIndex = dataIndex;
	}

	public IndexDataExchangeException(DataIndex dataIndex, String message)
	{
		super(message);
		this.dataIndex = dataIndex;
	}

	public IndexDataExchangeException(DataIndex dataIndex, Throwable cause)
	{
		super(cause);
		this.dataIndex = dataIndex;
	}

	public IndexDataExchangeException(DataIndex dataIndex, String message, Throwable cause)
	{
		super(message, cause);
		this.dataIndex = dataIndex;
	}

	public DataIndex getDataIndex()
	{
		return dataIndex;
	}

	protected void setDataIndex(DataIndex dataIndex)
	{
		this.dataIndex = dataIndex;
	}

}
