/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */

package org.datagear.analysis;

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
	 */
	InputStream getInputStream();

	/**
	 * 获取图标扩展名（比如：{@code png、jpeg}）
	 * 
	 * @return
	 */
	String getExtension();
}
