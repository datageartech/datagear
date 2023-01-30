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
