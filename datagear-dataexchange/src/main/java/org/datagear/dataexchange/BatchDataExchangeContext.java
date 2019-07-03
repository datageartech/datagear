package org.datagear.dataexchange;

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
	 * 取消指定的子数据交换。
	 * 
	 * @param subDataExchangeIds
	 * @return
	 */
	boolean[] cancel(String... subDataExchangeIds);
}
