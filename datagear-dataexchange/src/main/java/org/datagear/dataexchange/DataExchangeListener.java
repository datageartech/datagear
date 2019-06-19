/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据交换监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface DataExchangeListener
{
	/**
	 * 开始。
	 */
	void onStart();

	/**
	 * 异常。
	 * 
	 * @param e
	 */
	void onException(DataExchangeException e);

	/**
	 * 完成。
	 * <p>
	 * 无论是否有异常，此方法都将被调用。
	 * </p>
	 */
	void onFinish();
}
