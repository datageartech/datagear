/*
 * Copyright 2018-present datagear.tech
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

import java.util.Set;

/**
 * 批量数据交换结果。
 * 
 * @author datagear@163.com
 *
 */
public interface BatchDataExchangeResult
{
	/**
	 * 等待所有子数据交换执行完成。
	 * 
	 * @throws InterruptedException
	 */
	void waitForFinish() throws InterruptedException;

	/**
	 * 是否所有子数据交换已完成。
	 * 
	 * @return
	 */
	boolean isFinish();

	/**
	 * 获取当前还未提交的{@linkplain SubDataExchange}集合。
	 * 
	 * @return
	 */
	Set<SubDataExchange> getUnsubmits();

	/**
	 * 获取当前提交成功的{@linkplain SubDataExchange}数目。
	 * 
	 * @return
	 */
	int getSubmitSuccessCount();

	/**
	 * 获取当前提交成功的{@linkplain SubDataExchange}集合。
	 * 
	 * @return
	 */
	Set<SubDataExchange> getSubmitSuccesses();

	/**
	 * 获取当前提交失败的{@linkplain SubDataExchange}集合。
	 * 
	 * @return
	 */
	Set<SubDataExchange> getSubmitFails();

	/**
	 * 获取当前已取消的{@linkplain SubDataExchange}集合。
	 * 
	 * @return
	 */
	Set<SubDataExchange> getCancelleds();

	/**
	 * 获取当前执行完成的{@linkplain SubDataExchange}集合。
	 * 
	 * @return
	 */
	Set<SubDataExchange> getFinishes();

	/**
	 * 提交下一批具备执行条件的{@linkplain SubDataExchange}。
	 * <p>
	 * 具备执行条件：无任何依赖，或者，所有依赖都已执行完成。
	 * </p>
	 * <p>
	 * 返回空集合表示当前没有具备执行条件的{@linkplain SubDataExchange}。
	 * </p>
	 * 
	 * @return
	 */
	Set<SubDataExchange> submit();

	/**
	 * 取消指定{@linkplain SubDataExchange}。
	 * <p>
	 * 如果取消成功，将可在{@linkplain #getCancelleds()}中获取对应的{@linkplain SubDataExchange}。
	 * </p>
	 * 
	 * @param subDataExchangeId
	 */
	void cancel(String subDataExchangeId);
}
