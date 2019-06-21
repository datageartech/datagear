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

	/**
	 * 成功。
	 * <p>
	 * 此方法在所有子数据交换都提交完成后（可能提交成功，也可能提交失败）即会调用，因此并不能表明任何子数据交换成功。
	 * </p>
	 */
	@Override
	void onSuccess();

	/**
	 * 完成。
	 * <p>
	 * 此方法将在{@linkplain #onException(DataExchangeException)}或者{@linkplain #onSuccess()}之后被调用。
	 * </p>
	 * <p>
	 * 注意：在所有子数据交换都完成后，此方法才会执行。
	 * </p>
	 */
	@Override
	void onFinish();
}
