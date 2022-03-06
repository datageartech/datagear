/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

/**
 * 看板分享密码加密/解密接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DashboardSharePasswordCrypto
{
	/**
	 * 加密。
	 * <p>
	 * 对于{@code null}，应直接返回{@code null}；对于{@code ""}，应直接返回{@code ""}。
	 * </p>
	 * 
	 * @param password
	 * @return
	 */
	String encrypt(String password);

	/**
	 * 解密。
	 * <p>
	 * 对于{@code null}，应直接返回{@code null}；对于{@code ""}，应直接返回{@code ""}。
	 * </p>
	 * 
	 * @param password
	 * @return
	 */
	String decrypt(String password);
}
