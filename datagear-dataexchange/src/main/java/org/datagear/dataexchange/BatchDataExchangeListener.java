/*
 * Copyright 2018-2024 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.dataexchange;

/**
 * 批量数据交换监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface BatchDataExchangeListener extends DataExchangeListener
{
	/**
	 * 子数据交换提交成功。
	 * 
	 * @param subDataExchange
	 */
	void onSubmitSuccess(SubDataExchange subDataExchange);

	/**
	 * 子数据交换提交失败。
	 * 
	 * @param subDataExchange
	 * @param exception
	 */
	void onSubmitFail(SubDataExchange subDataExchange, SubmitFailException exception);

	/**
	 * 子数据交换取消。
	 * 
	 * @param subDataExchange
	 * @param reason
	 */
	void onCancel(SubDataExchange subDataExchange, CancelReason reason);

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
