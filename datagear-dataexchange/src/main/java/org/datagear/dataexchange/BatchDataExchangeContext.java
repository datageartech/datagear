package org.datagear.dataexchange;

import java.util.Set;

/**
 * 批量数据交换上下文。
 * 
 * @author datagear@163.com
 *
 */
public interface BatchDataExchangeContext
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
	 * 取消指定{@linkplain SubDataExchange}。
	 * <p>
	 * 如果取消成功，将可在{@linkplain #getCancelleds()}中获取对应的{@linkplain SubDataExchange}。
	 * </p>
	 * 
	 * @param subDataExchangeId
	 */
	void cancel(String subDataExchangeId);
}
