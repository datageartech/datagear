/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 数据导入接口类。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface DataImporter<T extends Import>
{
	/**
	 * 导入。
	 * 
	 * @param impt
	 * @return
	 * @throws DataImportException
	 */
	ImportResult impt(T impt) throws DataImportException;
}
