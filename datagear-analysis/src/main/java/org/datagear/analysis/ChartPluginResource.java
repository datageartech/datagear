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

package org.datagear.analysis;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图表插件资源。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginResource extends NameAware
{
	/**
	 * 获取资源输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;
	
	/**
	 * 获取上次修改时间。
	 * 
	 * @return
	 */
	long getLastModified();
}
