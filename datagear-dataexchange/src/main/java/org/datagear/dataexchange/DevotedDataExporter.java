/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 专职{@linkplain DataExporter}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface DevotedDataExporter<T extends Export> extends DataExporter<T>
{
	/**
	 * 是否支持指定{@linkplain Export}。
	 * 
	 * @param expt
	 * @return
	 */
	boolean supports(T expt);
}
