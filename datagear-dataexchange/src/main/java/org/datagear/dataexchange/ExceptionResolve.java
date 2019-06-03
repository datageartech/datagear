/*
 * Copyright (c) 2018 datagear.org. All Rights Reserved.
 */

package org.datagear.dataexchange;

/**
 * JDBC操作异常处理方式。
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
