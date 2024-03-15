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

package org.datagear.management.domain;

/**
 * 可克隆实体。
 * <p>
 * 此接口主要为无序列化缓存（比如进程内缓存）提供支持：先克隆实体再存入缓存，从缓存中取出实体后，先克隆再返回，避免实体被修改，导致缓存混乱。
 * </p>
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
	 * <ul>
	 * <li>如果属性值是共享对象（有独立生命周期），则仅需浅克隆（克隆引用）；</li>
	 * <li>如果属性值是私有对象，且其内部不包含共享对象（有独立生命周期），则仅需浅克隆（克隆引用）；</li>
	 * <li>如果属性值是私有对象，且其内部包含共享对象（有独立生命周期），则应对其继续应用上述克隆规则；</li>
	 * </ul>
	 * 
	 * @return
	 */
	Object clone();
}
