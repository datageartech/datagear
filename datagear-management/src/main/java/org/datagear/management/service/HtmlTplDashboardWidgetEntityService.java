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

import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.html.HtmlTplDashboardWidget;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;

/**
 * {@linkplain HtmlTplDashboardWidgetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlTplDashboardWidgetEntityService
		extends DataPermissionEntityService<String, HtmlTplDashboardWidgetEntity>, CreateUserEntityService,
		AnalysisProjectAwareEntityService<HtmlTplDashboardWidgetEntity>
{
	/**
	 * 获取渲染器。
	 * 
	 * @return
	 */
	HtmlTplDashboardWidgetRenderer getHtmlTplDashboardWidgetRenderer();

	/**
	 * 获取资源管理器。
	 * 
	 * @return
	 */
	TplDashboardWidgetResManager getTplDashboardWidgetResManager();

	/**
	 * 获取可用于执行分析的{@linkplain HtmlTplDashboardWidget}。
	 * 
	 * @param user
	 * @param id
	 * @return
	 * @throws PermissionDeniedException
	 */
	HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidget(User user, String id) throws PermissionDeniedException;
}
