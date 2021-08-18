/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.management.service.impl;

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
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
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

	public HtmlTplDashboardWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
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
		return super.updateCreateUserId(oldUserId, newUserId);
	}

	@Override
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter,
			String analysisProjectId)
	{
		return pagingQueryForAnalysisProjectId(user, pagingQuery, dataFilter, analysisProjectId);
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.htmlTplDashboardWidgetRenderer.getTemplateDashboardWidgetResManager().delete(id);

		return deleted;
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
		super.addDataPermissionParameters(params, user);
		params.put(AnalysisProjectAwareEntity.DATA_PERMISSION_PARAM_RESOURCE_TYPE_ANALYSIS_PROJECT,
				AnalysisProject.AUTHORIZATION_RESOURCE_TYPE);
	}

	@Override
	protected String getSqlNamespace()
	{
		return SQL_NAMESPACE;
	}
}
