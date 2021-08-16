/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.domain;

/**
 * 可克隆实体。
 * 
 * @author datagear@163.com
 *
 */
public interface CloneableEntity extends Cloneable
{
	/**
	 * 克隆。
	 * <p>
	 * 实现方法应遵循如下克隆规则：
	 * </p>
	 * <p>
	 * 如果属性值是实体对象，则应仅克隆引用；否则，应对属性值进行深度克隆。
	 * </p>
	 * 
	 * @return
	 */
	Object clone();
}
