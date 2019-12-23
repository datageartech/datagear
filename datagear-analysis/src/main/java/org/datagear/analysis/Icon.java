/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图标。
 * 
 * @author datagear@163.com
 *
 */
public interface Icon
{
	/**
	 * 获取图标类型：{@code png}、{@code jpeg}等，未知则返回空字符串。
	 * 
	 * @return
	 */
	String getType();

	/**
	 * 获取图标输入流。
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
