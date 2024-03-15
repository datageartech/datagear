/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.management.service.impl;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.TplDashboardWidgetResManager;
import org.datagear.analysis.support.html.HtmlTplDashboardWidgetRenderer;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.HtmlTplDashboardWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.HtmlTplDashboardWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.UserService;
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

	private TplDashboardWidgetResManager tplDashboardWidgetResManager;

	private AnalysisProjectService analysisProjectService;

	private UserService userService;

	public HtmlTplDashboardWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer,
			TplDashboardWidgetResManager tplDashboardWidgetResManager,
			AnalysisProjectService analysisProjectService,
			UserService userService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
		this.tplDashboardWidgetResManager = tplDashboardWidgetResManager;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
	}

	public HtmlTplDashboardWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			HtmlTplDashboardWidgetRenderer htmlTplDashboardWidgetRenderer,
			TplDashboardWidgetResManager tplDashboardWidgetResManager,
			AnalysisProjectService analysisProjectService,
			UserService userService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.htmlTplDashboardWidgetRenderer = htmlTplDashboardWidgetRenderer;
		this.tplDashboardWidgetResManager = tplDashboardWidgetResManager;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
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

	public TplDashboardWidgetResManager getTplDashboardWidgetResManager()
	{
		return tplDashboardWidgetResManager;
	}

	public void setTplDashboardWidgetResManager(
			TplDashboardWidgetResManager tplDashboardWidgetResManager)
	{
		this.tplDashboardWidgetResManager = tplDashboardWidgetResManager;
	}

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	public HtmlTplDashboardWidgetEntity getHtmlTplDashboardWidget(User user, String id)
	{
		HtmlTplDashboardWidgetEntity dashboard = getById(user, id);

		if (dashboard != null)
		{
			dashboard.setRenderer(this.htmlTplDashboardWidgetRenderer);
			dashboard.setResManager(this.tplDashboardWidgetResManager);
		}

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
	public int updateCreateUserId(String[] oldUserIds, String newUserId)
	{
		return super.updateCreateUserId(oldUserIds, newUserId);
	}

	@Override
	public PagingData<HtmlTplDashboardWidgetEntity> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter,
			String analysisProjectId)
	{
		return pagingQueryForAnalysisProjectId(user, pagingQuery, dataFilter, analysisProjectId, true);
	}

	@Override
	public void authorizationUpdated(String... analysisProjects)
	{
		permissionCacheInvalidate();
	}

	@Override
	protected HtmlTplDashboardWidgetEntity postProcessGet(HtmlTplDashboardWidgetEntity obj)
	{
		inflateAnalysisProjectAwareEntity(obj, this.analysisProjectService);
		inflateCreateUserEntity(obj, this.userService);

		return super.postProcessGet(obj);
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.tplDashboardWidgetResManager.delete(id);

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
