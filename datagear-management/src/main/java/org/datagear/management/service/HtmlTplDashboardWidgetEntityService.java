/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

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
		extends DataPermissionEntityService<String, HtmlTplDashboardWidgetEntity>, CreateUserEntityService
{
	/**
	 * 获取渲染器。
	 * 
	 * @return
	 */
	HtmlTplDashboardWidgetRenderer getHtmlTplDashboardWidgetRenderer();

	/**
	 * 获取可用于执行分析的{@linkplain HtmlTplDashboardWidget}。
	 * 
	 * @param user
	 * @param id
	 * @return
	 */
	HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidget(User user, String id);
}
