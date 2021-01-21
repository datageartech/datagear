/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange.support;

/**
 * JSON数据格式。
 * 
 * @author datagear@163.com
 *
 */
public enum JsonDataFormat
{
	/** 表对象，格式为：{"table_name" : [{...}, {...}]} */
	TABLE_OBJECT,

	/** 行数组：格式为：[{...}, {...}] */
	ROW_ARRAY
}
