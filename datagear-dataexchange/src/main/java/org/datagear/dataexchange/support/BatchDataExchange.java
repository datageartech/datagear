/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange.support;

import java.util.List;

import org.datagear.dataexchange.ConnectionFactory;
import org.datagear.dataexchange.DataExchange;

/**
 * 批量数据交换。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public abstract class BatchDataExchange<T extends DataExchange> extends DataExchange
{
	public BatchDataExchange()
	{
		super();
	}

	public BatchDataExchange(ConnectionFactory connectionFactory)
	{
		super(connectionFactory);
	}

	/**
	 * 拆分导入。
	 * 
	 * @return
	 */
	public abstract List<T> split();
}
