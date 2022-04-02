/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.util;

/**
 * 全局常量。
 * 
 * @author datagear@163.com
 *
 */
public final class Global
{
	private Global()
	{
		throw new UnsupportedOperationException();
	}

	/** 当前版本号 */
	public static final String VERSION = "3.0.1";

	/** 中文产品名称 */
	public static final String PRODUCT_NAME_ZH = "数据齿轮";

	/** 英文产品名称 */
	public static final String PRODUCT_NAME_EN = "DataGear";

	/** 官网 */
	public static final String WEB_SITE = "http://www.datagear.tech";
}
