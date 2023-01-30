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

import java.util.Collection;

import org.datagear.management.service.AuthorizationListener;

/**
 * 打包一起的{@linkplain AuthorizationListener}。
 * 
 * @author datagear@163.com
 *
 */
public class BundleAuthorizationListener implements AuthorizationListener
{
	private Collection<AuthorizationListener> authorizationListeners = null;

	public BundleAuthorizationListener()
	{
		super();
	}

	public BundleAuthorizationListener(Collection<AuthorizationListener> authorizationListeners)
	{
		super();
		this.authorizationListeners = authorizationListeners;
	}

	public Collection<AuthorizationListener> getAuthorizationListeners()
	{
		return authorizationListeners;
	}

	public void setAuthorizationListeners(Collection<AuthorizationListener> authorizationListeners)
	{
		this.authorizationListeners = authorizationListeners;
	}

	@Override
	public void authorizationUpdated(String resourceType, String... resources)
	{
		if (this.authorizationListeners == null)
			return;

		for (AuthorizationListener al : this.authorizationListeners)
			al.authorizationUpdated(resourceType, resources);
	}

	@Override
	public void permissionUpdated()
	{
		if (this.authorizationListeners == null)
			return;

		for (AuthorizationListener al : this.authorizationListeners)
			al.permissionUpdated();
	}
}
