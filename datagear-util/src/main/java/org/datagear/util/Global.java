/*
 * Copyright 2018-present datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
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
	public static final String VERSION = "5.2.0";

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

	/** 官网-https */
	public static final String WEB_SITE_HTTPS = "https://www.datagear.tech";
	
	/** 带下划线的大写名称缩写 */
	public static final String NAME_SHORT_UCUS = "DG_";
}
