/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据交换服务接口。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface DataExchangeService<T extends DataExchange>
{
	/**
	 * 进行数据交换。
	 * 
	 * @param dataExchange
	 * @throws DataExchangeException
	 */
	void exchange(T dataExchange) throws DataExchangeException;
}
