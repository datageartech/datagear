/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
public interface ChartPluginResource
{
	/**
	 * 获取资源名称。
	 * 
	 * @return
	 */
	String getName();
	
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
