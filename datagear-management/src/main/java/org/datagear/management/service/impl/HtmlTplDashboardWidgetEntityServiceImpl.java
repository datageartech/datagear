/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain HtmlTplDashboardWidgetEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlTplDashboardWidgetEntityServiceImpl
		extends AbstractMybatisDataPermissionEntityService<String, HtmlTplDashboardWidgetEntity>
		implements HtmlTplDashboardWidgetEntityService
{
	protected static final String SQL_NAMESPACE = HtmlTplDashboardWidgetEntity.class.getName();

	private HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer;

	private AuthorizationService authorizationService;

	public HtmlTplDashboardWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
		this.authorizationService = authorizationService;
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer, AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
		this.authorizationService = authorizationService;
	}

	@Override
	public HtmlTplDashboardWidgetRenderer getHtmlTplDashboardWidgetRenderer()
	{
		return htmlTplDashboardWidgetRenderer;
	}

	public void setHtmlTplDashboardWidgetRenderer(HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer)
	{
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
	}

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	@Override
	public HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidget(User user, String id)
	{
		HtmlTplDashboardWidgetEntity dashboard = getById(user, id);

		if (dashboard != null)
			dashboard.setRenderer(this.htmlTplDashboardWidgetRenderer);

		return dashboard;
	}

	@Override
	public String getResourceType()
	{
		return HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public HtmlTplDashboardWidgetEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	@Override
	public int updateCreateUserId(String oldUserId, String newUserId)
	{
		Map<String, Object> params = buildParamMap();
		addIdentifierQuoteParameter(params);
		params.put("oldUserId", oldUserId);
		params.put("newUserId", newUserId);

		return updateMybatis("updateCreateUserId", params);
	}

	@Override
	protected boolean add(HtmlTplDashboardWidgetEntity entity, Map<String, Object> params)
	{
		boolean success = super.add(entity, params);

		return success;
	}

	@Override
	protected boolean update(HtmlTplDashboardWidgetEntity entity, Map<String, Object> params)
	{
		boolean success = super.update(entity, params);

		return success;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
		{
			this.authorizationService.deleteByResource(HtmlTplDashboardWidgetEntity.AUTHORIZATION_RESOURCE_TYPE, id);
			this.htmlTplDashboardWidgetRenderer.getTemplateDashboardWidgetResManager().delete(id);
		}

		return deleted;
	}

	@Override
	protected void postProcessSelects(List<HtmlTplDashboardWidgetEntity> list)
	{
		// XXX 查询操作仅用于展示，不必完全加载
		// super.postProcessSelects(list);
	}

	@Override
	protected void checkInput(HtmlTplDashboardWidgetEntity entity)
	{
		if (isBlank(entity.getId()) || isEmpty(entity.getTemplates()))
			throw new IllegalArgumentException();
	}

	@Override
	protected void addDataPermissionParameters(Map<String, Object> params, User user)
	{
		params.put(AnalysisProjectAwareEntity.DATA_PERMISSION_PARAM_RESOURCE_TYPE_ANALYSIS_PROJECT,
				AnalysisProject.AUTHORIZATION_RESOURCE_TYPE);
		addDataPermissionParameters(params, user, getResourceType(), false, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
