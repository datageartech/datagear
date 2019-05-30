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
public interface DataExporter<T extends DataExport>
{
	/**
	 * 导出。
	 * 
	 * @param expt
	 * @return
	 * @throws DataExportException
	 */
	DataExportResult expt(T expt) throws DataExportException;
}
