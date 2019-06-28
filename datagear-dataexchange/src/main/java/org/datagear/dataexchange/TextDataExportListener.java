/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本导出监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface TextDataExportListener extends DataExchangeListener
{
	/**
	 * 指定索引的数据导出成功。
	 * 
	 * @param dataIndex
	 */
	void onSuccess(int dataIndex);

	/**
	 * 读取列值出现异常时导出文本值被设置为{@code null}。
	 * 
	 * @param dataIndex
	 * @param columnName
	 * @param e
	 */
	void onSetNullTextValue(int dataIndex, String columnName, DataExchangeException e);
}
