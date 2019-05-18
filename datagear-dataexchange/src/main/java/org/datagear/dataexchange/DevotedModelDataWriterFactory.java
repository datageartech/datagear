package org.datagear.dataexchange;

/**
 * 专职{@linkplain ModelDataWriterFactory}。
 * @author datagear@163.com
 *
 */
public interface DevotedModelDataWriterFactory extends ModelDataWriterFactory
{
	/**
	 * 是否支持指定{@linkplain Export}。
	 * @param expt
	 * @return
	 */
	boolean supports(Export expt);
}
