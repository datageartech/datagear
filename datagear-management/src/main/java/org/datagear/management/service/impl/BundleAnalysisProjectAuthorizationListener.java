/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import java.util.Collection;

import org.datagear.management.service.AnalysisProjectAuthorizationListener;

/**
 * 打包一起的{@linkplain AnalysisProjectAuthorizationListener}。
 * 
 * @author datagear@163.com
 *
 */
public class BundleAnalysisProjectAuthorizationListener implements AnalysisProjectAuthorizationListener
{
	private Collection<AnalysisProjectAuthorizationListener> analysisProjectAuthorizationListeners = null;

	public BundleAnalysisProjectAuthorizationListener()
	{
		super();
	}

	public BundleAnalysisProjectAuthorizationListener(
			Collection<AnalysisProjectAuthorizationListener> analysisProjectAuthorizationListeners)
	{
		super();
		this.analysisProjectAuthorizationListeners = analysisProjectAuthorizationListeners;
	}

	public Collection<AnalysisProjectAuthorizationListener> getAnalysisProjectAuthorizationListeners()
	{
		return analysisProjectAuthorizationListeners;
	}

	public void setAnalysisProjectAuthorizationListeners(
			Collection<AnalysisProjectAuthorizationListener> analysisProjectAuthorizationListeners)
	{
		this.analysisProjectAuthorizationListeners = analysisProjectAuthorizationListeners;
	}

	@Override
	public void authorizationUpdated(String... analysisProjects)
	{
		if (this.analysisProjectAuthorizationListeners == null)
			return;

		for (AnalysisProjectAuthorizationListener al : this.analysisProjectAuthorizationListeners)
			al.authorizationUpdated(analysisProjects);
	}
}
