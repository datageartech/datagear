/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 文本导出监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface TextDataExportListener extends DataExportListener
{
	/**
	 * 读取列值出现异常时导出文本值被设置为{@code null}。
	 * 
	 * @param dataIndex
	 * @param columnName
	 * @param e
	 */
	void onSetNullTextValue(DataIndex dataIndex, String columnName, DataExchangeException e);
}
