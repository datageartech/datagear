/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导出接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface DataExporter<T extends Export>
{
	/**
	 * 导出。
	 * 
	 * @param expt
	 * @return
	 * @throws DataExportException
	 */
	ExportResult expt(T expt) throws DataExportException;
}
