/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导出报告。
 * 
 * @author datagear@163.com
 *
 */
public interface DataExportReporter
{
	/**
	 * 报告导出异常。
	 * 
	 * @param e
	 */
	void report(DataExportException e);
}
