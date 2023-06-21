/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.util.cache;

import org.springframework.cache.Cache;

/**
 * 本地缓存（进程内缓存）。
 * <p>
 * 此类没有特别的接口方法，仅用于标识本地缓存用途。
 * </p>
 * <p>
 * 某些应用场景仅需要本地缓存即可（比如缓存本地文件相关的信息），此时可以使用此标识类。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface LocalCache extends Cache
{

}
