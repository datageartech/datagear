/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 专职{@linkplain DataExchangeService}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface DevotedDataExchangeService<T extends DataExchange> extends DataExchangeService<T>
{
	/**
	 * 是否支持指定{@linkplain DataExchange}。
	 * 
	 * @param dataExchange
	 * @return
	 */
	boolean supports(T dataExchange);
}
