/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 
 */
package org.datagear.management.service;

import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.User;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;

/**
 * {@linkplain AnalysisProjectAwareEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface AnalysisProjectAwareEntityService<T>
{
	String QUERY_PARAM_ANALYSIS_PROJECT_ID = "_analysisProjectId";

	/**
	 * 授权分页查询。
	 * 
	 * @param user
	 *            操作用户
	 * @param pagingQuery
	 * @param dataFilter
	 * @param analysisProjectId
	 *            允许为{@code null}
	 * @return
	 */
	PagingData<T> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter, String analysisProjectId);
}
