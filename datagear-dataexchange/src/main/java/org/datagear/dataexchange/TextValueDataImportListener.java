/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 文本值导入监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface TextValueDataImportListener extends DataImportListener
{
	/**
	 * {@code rawColumnValue}非法时列值被设置为{@code null}。
	 * 
	 * @param dataIndex
	 * @param columnName
	 * @param rawColumnValue
	 * @param e
	 */
	void onSetNullColumnValue(DataIndex dataIndex, String columnName, String rawColumnValue, DataExchangeException e);
}
