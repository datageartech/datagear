/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
