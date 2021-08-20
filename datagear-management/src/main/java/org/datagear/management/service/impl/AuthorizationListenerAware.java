/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import org.datagear.management.service.AuthorizationListener;

/**
 * {@linkplain AuthorizationListener}相关类。
 * 
 * @author datagear@163.com
 *
 */
public interface AuthorizationListenerAware
{
	/**
	 * 获取{@linkplain AuthorizationListener}。
	 * 
	 * @return 可能为{@code null}
	 */
	AuthorizationListener getAuthorizationListener();

	/**
	 * 设置{@linkplain AuthorizationListener}。
	 * 
	 * @param authorizationListener
	 *            允许为{@code null}
	 */
	void setAuthorizationListener(AuthorizationListener authorizationListener);
}
