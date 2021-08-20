/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.AnalysisProject;

/**
 * {@linkplain AnalysisProject}授权监听器。
 * 
 * @author datagear@163.com
 *
 */
public interface AnalysisProjectAuthorizationListener
{
	/**
	 * 指定ID的{@linkplain AnalysisProject}授权已更新。
	 * 
	 * @param analysisProjects
	 */
	void authorizationUpdated(String... analysisProjects);

}
