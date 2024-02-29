/*
 * Copyright 2018-2024 datagear.tech
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
