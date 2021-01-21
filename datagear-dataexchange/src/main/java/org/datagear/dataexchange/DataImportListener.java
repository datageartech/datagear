/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 数据导入监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface DataImportListener extends DataExchangeListener
{
	/**
	 * 指定索引的数据导入成功。
	 * 
	 * @param dataIndex
	 */
	void onSuccess(DataIndex dataIndex);

	/**
	 * 指定索引的数据因为异常而被忽略。
	 * 
	 * @param dataIndex
	 */
	void onIgnore(DataIndex dataIndex, DataExchangeException e);

}
