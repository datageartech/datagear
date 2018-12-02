/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.format;

import java.util.Date;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * 抽象日期相关的{@linkplain Formatter}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractDateFormatter<T extends Date> implements Formatter<T>
{
	/**
	 * 获取解析的格式。
	 * 
	 * @param locale
	 * @return
	 */
	public abstract String getParsePattern(Locale locale);
}
