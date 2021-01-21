/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 值导入监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface ValueDataImportListener extends DataImportListener
{
	/**
	 * {@code columnValue}非法时列值被设置为{@code null}。
	 * 
	 * @param dataIndex
	 * @param columnName
	 * @param columnValue
	 * @param e
	 */
	void onSetNullColumnValue(DataIndex dataIndex, String columnName, Object columnValue, DataExchangeException e);
}
