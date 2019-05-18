package org.datagear.dataexchange;

/**
 * 专职{@linkplain ModelDataReaderFactory}。
 * @author datagear@163.com
 *
 */
public interface DevotedModelDataReaderFactory extends ModelDataReaderFactory
{
	/**
	 * 是否支持指定{@linkplain Import}。
	 * @param impt
	 * @return
	 */
	boolean supports(Import impt);
}
