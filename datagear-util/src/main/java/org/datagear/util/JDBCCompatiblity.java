/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JDBC驱动兼容标注。
 * <p>
 * 为了兼容某些JDBC驱动程序，部分代码会有特殊逻辑，可使用此类标注说明。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
@Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
		ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE })
@Retention(value = RetentionPolicy.SOURCE)
public @interface JDBCCompatiblity
{
	/**
	 * 原因。
	 * 
	 * @return
	 */
	String value() default "";
}
