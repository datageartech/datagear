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
public interface AnalysisProjectAwareEntityService<T> extends AnalysisProjectAuthorizationListener
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
