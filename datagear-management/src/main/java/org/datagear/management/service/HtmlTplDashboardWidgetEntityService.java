/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
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
