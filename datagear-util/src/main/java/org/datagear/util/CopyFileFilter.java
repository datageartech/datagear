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

package org.datagear.util;

import java.io.File;

/**
 * 拷贝文件过滤器。
 * 
 * @author datagear@163.com
 *
 */
@FunctionalInterface
public interface CopyFileFilter
{
	/**
	 * 是否允许拷贝。
	 * 
	 * @param from
	 *            文件、目录
	 * @param to
	 *            文件、目录，可能还不存在
	 * @return
	 */
	boolean accept(File from, File to);
}
