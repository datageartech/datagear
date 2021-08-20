/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

import org.datagear.management.service.AnalysisProjectAuthorizationListener;

/**
 * {@linkplain AnalysisProjectAuthorizationListener}相关类。
 * 
 * @author datagear@163.com
 *
 */
public interface AnalysisProjectAuthorizationListenerAware
{
	/**
	 * 获取{@linkplain AnalysisProjectAuthorizationListener}。
	 * 
	 * @return 可能为{@code null}
	 */
	AnalysisProjectAuthorizationListener getAnalysisProjectAuthorizationListener();

	/**
	 * 设置{@linkplain AnalysisProjectAuthorizationListener}。
	 * 
	 * @param analysisProjectAuthorizationListener 允许为{@code null}
	 */
	void setAnalysisProjectAuthorizationListener(
			AnalysisProjectAuthorizationListener analysisProjectAuthorizationListener);
}
