/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * 专职{@linkplain DataImporter}。
 * 
 * @author datagear@163.com
 *
 * @param <T>
 */
public interface DevotedDataImporter<T extends DataImport> extends DataImporter<T>
{
	/**
	 * 是否支持指定{@linkplain DataImport}。
	 * 
	 * @param impt
	 * @return
	 */
	boolean supports(T impt);
}
