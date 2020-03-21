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
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetHtmlRenderer;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
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

	private HtmlTplDashboardWidgetRenderer<HtmlRenderContext> htmlTplDashboardWidgetRenderer;

	private AuthorizationService authorizationService;

	public HtmlTplDashboardWidgetEntityServiceImpl()
	{
		super();
	}

	@SuppressWarnings("unchecked")
	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory,
			HtmlTplDashboardWidgetRenderer<? extends HtmlRenderContext> htmlTplDashboardWidgetRenderer,
			AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.htmlTplDashboardWidgetRenderer = (HtmlTplDashboardWidgetHtmlRenderer<HtmlRenderContext>) htmlTplDashboardWidgetRenderer;
		this.authorizationService = authorizationService;
	}

	@SuppressWarnings("unchecked")
	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate,
			HtmlTplDashboardWidgetRenderer<? extends HtmlRenderContext> htmlTplDashboardWidgetRenderer,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
		this.htmlTplDashboardWidgetRenderer = (HtmlTplDashboardWidgetHtmlRenderer<HtmlRenderContext>) htmlTplDashboardWidgetRenderer;
		this.authorizationService = authorizationService;
	}

	@Override
	public HtmlTplDashboardWidgetRenderer<HtmlRenderContext> getHtmlTplDashboardWidgetRenderer()
	{
		return htmlTplDashboardWidgetRenderer;
	}

	public void setHtmlTplDashboardWidgetRenderer(
			HtmlTplDashboardWidgetRenderer<HtmlRenderContext> htmlTplDashboardWidgetRenderer)
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
	protected void postProcessSelect(HtmlTplDashboardWidgetEntity obj)
	{
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
		addDataPermissionParameters(params, user, getResourceType(), false, true);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
