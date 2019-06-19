/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本导入监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface TextDataImportListener extends DataExchangeListener
{
	/**
	 * 指定索引的数据导入成功。
	 * 
	 * @param dataIndex
	 */
	void onSuccess(int dataIndex);

	/**
	 * 指定索引的数据导入失败。
	 * 
	 * @param dataIndex
	 */
	void onFail(int dataIndex);
}
