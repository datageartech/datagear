/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
