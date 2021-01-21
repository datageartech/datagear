/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service;

import org.datagear.management.domain.AnalysisProject;

/**
 * {@linkplain AnalysisProject}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface AnalysisProjectService
		extends DataPermissionEntityService<String, AnalysisProject>, CreateUserEntityService
{

}
