/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

/**
 * {@linkplain ResultDataFormat}引用类。
 * 
 * @author datagear@163.com
 *
 */
public interface ResultDataFormatAware
{
	/**
	 * 获取{@linkplain ResultDataFormat}。
	 * 
	 * @return 返回{@code null}表示没有设置
	 */
	ResultDataFormat getResultDataFormat();

	/**
	 * 设置{@linkplain ResultDataFormat}。
	 * 
	 * @param format
	 */
	void setResultDataFormat(ResultDataFormat format);
}
