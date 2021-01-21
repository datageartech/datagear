/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.dataexchange;

/**
 * 数据交换异常处理方式。
 * 
 * @author datagear@163.com
 *
 */
public enum ExceptionResolve
{
	/** 提交并终止 */
	ABORT,

	/** 忽略并继续 */
	IGNORE,

	/** 回滚并终止 */
	ROLLBACK
}
