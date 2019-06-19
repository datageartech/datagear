/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 批量数据交换监听器。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface BatchDataExchangeListener<T extends DataExchange> extends DataExchangeListener
{
	/**
	 * 子数据交换提交成功。
	 * 
	 * @param subDataExchange
	 * @param subDataExchangeIndex
	 */
	void onSubmitSuccess(T subDataExchange, int subDataExchangeIndex);

	/**
	 * 子数据交换提交失败。
	 * 
	 * @param subDataExchange
	 * @param subDataExchangeIndex
	 * @param cause
	 */
	void onSubmitFail(T subDataExchange, int subDataExchangeIndex, Throwable cause);

	/**
	 * 子数据交换取消。
	 * 
	 * @param subDataExchange
	 * @param subDataExchangeIndex
	 */
	void onCancel(T subDataExchange, int subDataExchangeIndex);
}
