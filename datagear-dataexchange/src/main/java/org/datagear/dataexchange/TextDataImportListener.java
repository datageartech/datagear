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
	 * 指定索引的数据因为异常而被忽略。
	 * 
	 * @param dataIndex
	 */
	void onIgnore(int dataIndex, DataExchangeException e);

	/**
	 * {@code rawColumnValue}非法时列值被设置为{@code null}。
	 * 
	 * @param dataIndex
	 * @param columnName
	 * @param rawColumnValue
	 * @param e
	 */
	void onSetNullColumnValue(int dataIndex, String columnName, String rawColumnValue, DataExchangeException e);
}
