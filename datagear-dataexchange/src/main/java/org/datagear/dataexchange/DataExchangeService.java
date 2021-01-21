/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 数据交换服务。
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
