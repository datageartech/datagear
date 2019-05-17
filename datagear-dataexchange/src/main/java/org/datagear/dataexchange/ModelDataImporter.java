/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 模型数据导入接口类。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelDataImporter
{
	/**
	 * 导入。
	 * 
	 * @param impt
	 * @return
	 */
	ImportResult impt(Import impt);
}
