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
	 * 获取图标输入流。
	 * 
	 * @return
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;
}
