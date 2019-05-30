/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导入报告。
 * 
 * @author datagear@163.com
 *
 */
public interface DataImportReporter
{
	/**
	 * 报告导入异常。
	 * 
	 * @param e
	 */
	void report(DataImportException e);
}
