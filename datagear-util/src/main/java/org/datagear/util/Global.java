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
	public static final String VERSION = "4.5.0";

	/** 中文产品名称 */
	public static final String PRODUCT_NAME_ZH = "数据齿轮";

	/** 英文产品名称 */
	public static final String PRODUCT_NAME_EN = "DataGear";

	/** 英文产品名称，全小写 */
	public static final String PRODUCT_NAME_EN_LC = PRODUCT_NAME_EN.toLowerCase();

	/** 英文产品名称，全大写 */
	public static final String PRODUCT_NAME_EN_UC = PRODUCT_NAME_EN.toUpperCase();

	/** 官网 */
	public static final String WEB_SITE = "http://www.datagear.tech";

	/** 版权声明 */
	public static final String COPYRIGHT = "© 2018 datagear.tech";
}
