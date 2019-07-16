/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导出监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface DataExportListener extends DataExchangeListener
{
	/**
	 * 指定索引的数据导出成功。
	 * 
	 * @param dataIndex
	 */
	void onSuccess(DataIndex dataIndex);
}
