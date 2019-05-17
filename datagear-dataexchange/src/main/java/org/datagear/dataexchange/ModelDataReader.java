/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

import java.util.NoSuchElementException;

import org.datagear.model.Model;

/**
 * 模型数据输入流。
 * 
 * @author datagear@163.com
 *
 */
public interface ModelDataReader
{
	/**
	 * 读取{@linkplain Model}。
	 * 
	 * @return
	 */
	Model readModel();

	/**
	 * 是否还有可读的数据。
	 * 
	 * @return
	 */
	boolean hasNextData();

	/**
	 * 读取下一条数据。
	 * 
	 * @return
	 * @throws NoSuchElementException
	 *             当没有可读数据时，抛出此异常
	 */
	Object readNextData() throws NoSuchElementException;

	/**
	 * 读取下一批次数据。
	 * 
	 * @param datas
	 *            待读入的对象数组
	 * @return 实际读取的数据条目数
	 * @throws NoSuchElementException
	 */
	int readNextDatas(Object[] datas) throws NoSuchElementException;
}
