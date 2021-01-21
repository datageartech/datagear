/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
	 * <p>
	 * 数据交换异常交由此处理后，不会再向上抛出。
	 * </p>
	 * 
	 * @param e
	 */
	void onException(DataExchangeException e);

	/**
	 * 成功。
	 */
	void onSuccess();

	/**
	 * 完成。
	 * <p>
	 * 此方法将在{@linkplain #onException(DataExchangeException)}或者{@linkplain #onSuccess()}之后被调用。
	 * </p>
	 */
	void onFinish();
}
