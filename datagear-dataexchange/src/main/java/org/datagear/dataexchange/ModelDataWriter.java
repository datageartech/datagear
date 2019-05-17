/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import org.datagear.model.Model;

/**
 * 模型数据输出流。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelDataWriter
{
	/**
	 * 写入{@linkplain Model}。
	 * 
	 * @return
	 */
	void writeModel(Model model);

	/**
	 * 写入一条数据。
	 * 
	 * @param data
	 */
	void writeData(Object data);

	/**
	 * 写入一批数据。
	 * 
	 * @param datas
	 */
	void writeDatas(Object[] datas);

	/**
	 * 关闭输出流。
	 */
	void close();
}
