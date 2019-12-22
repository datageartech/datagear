/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.SqlDataSetFactory;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.SqlDataSetFactoryEntityService;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * {@linkplain HtmlChartWidgetEntityService}实现类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetEntityServiceImpl
		extends AbstractMybatisDataPermissionEntityService<String, HtmlChartWidgetEntity>
		implements HtmlChartWidgetEntityService
{
	protected static final String SQL_NAMESPACE = HtmlChartWidgetEntity.class.getName();

	private ChartPluginManager chartPluginManager;

	private SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService;

	private AuthorizationService authorizationService;

	public HtmlChartWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, ChartPluginManager chartPluginManager,
			SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.chartPluginManager = chartPluginManager;
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
		this.authorizationService = authorizationService;
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate,
			ChartPluginManager chartPluginManager, SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
		this.chartPluginManager = chartPluginManager;
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
		this.authorizationService = authorizationService;
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
	}

	public SqlDataSetFactoryEntityService getSqlDataSetFactoryEntityService()
	{
		return sqlDataSetFactoryEntityService;
	}

	public void setSqlDataSetFactoryEntityService(SqlDataSetFactoryEntityService sqlDataSetFactoryEntityService)
	{
		this.sqlDataSetFactoryEntityService = sqlDataSetFactoryEntityService;
	}

	public AuthorizationService getAuthorizationService()
	{
		return authorizationService;
	}

	public void setAuthorizationService(AuthorizationService authorizationService)
	{
		this.authorizationService = authorizationService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RenderContext> ChartWidget<T> getChartWidget(String id)
	{
		HtmlChartWidgetEntity entity = getById(id);

		if (entity == null)
			return null;

		setHtmlChartPlugin(entity);

		SqlDataSetFactory[] sqlDataSetFactories = this.sqlDataSetFactoryEntityService
				.getSqlDataSetFactories(entity.getId());

		entity.setDataSetFactories(sqlDataSetFactories);

		return (ChartWidget<T>) entity;
	}

	@Override
	public String getResourceType()
	{
		return HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE;
	}

	@Override
	public HtmlChartWidgetEntity getByStringId(User user, String id) throws PermissionDeniedException
	{
		return super.getById(user, id);
	}

	@Override
	protected boolean add(HtmlChartWidgetEntity entity, Map<String, Object> params)
	{
		boolean success = super.add(entity, params);

		if (success)
			saveWidgetDataSetFactoryRelations(entity);

		return success;
	}

	@Override
	protected boolean update(HtmlChartWidgetEntity entity, Map<String, Object> params)
	{
		boolean success = super.update(entity, params);

		if (success)
			saveWidgetDataSetFactoryRelations(entity);

		return success;
	}

	@Override
	protected boolean deleteById(String id, Map<String, Object> params)
	{
		boolean deleted = super.deleteById(id, params);

		if (deleted)
			this.authorizationService.deleteByResource(HtmlChartWidgetEntity.AUTHORIZATION_RESOURCE_TYPE, id);

		return deleted;
	}

	protected void saveWidgetDataSetFactoryRelations(HtmlChartWidgetEntity entity)
	{
		deleteMybatis("deleteDataSetRelationById", entity.getId());

		List<WidgetDataSetFactoryRelation> relations = WidgetDataSetFactoryRelation.valuesOf(entity);

		if (!relations.isEmpty())
		{
			for (WidgetDataSetFactoryRelation relation : relations)
				insertMybatis("insertDataSetRelation", relation);
		}
	}

	@Override
	protected void postProcessSelects(List<HtmlChartWidgetEntity> list)
	{
		// 查询操作仅用于展示，不必完全加载
		// super.postProcessSelects(list);
	}

	@Override
	protected void postProcessSelect(HtmlChartWidgetEntity obj)
	{
		if (obj == null)
			return;

		setHtmlChartPlugin(obj);

		SqlDataSetFactoryEntity[] sqlDataSetFactories = this.sqlDataSetFactoryEntityService
				.getSqlDataSetFactoryEntities(obj.getId());

		obj.setSqlDataSetFactoryEntities(sqlDataSetFactories);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setHtmlChartPlugin(HtmlChartWidgetEntity obj)
	{
		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = obj.getHtmlChartPlugin();

		if (htmlChartPlugin != null)
		{
			htmlChartPlugin = (HtmlChartPlugin<HtmlRenderContext>) (ChartPlugin) this.chartPluginManager
					.get(htmlChartPlugin.getId());

			obj.setHtmlChartPlugin(htmlChartPlugin);
		}

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

	public static class WidgetDataSetFactoryRelation
	{
		private String widgetId;

		private String dataSetFactoryId;

		private int order;

		public WidgetDataSetFactoryRelation()
		{
			super();
		}

		public WidgetDataSetFactoryRelation(String widgetId, String dataSetFactoryId, int order)
		{
			super();
			this.widgetId = widgetId;
			this.dataSetFactoryId = dataSetFactoryId;
			this.order = order;
		}

		public String getWidgetId()
		{
			return widgetId;
		}

		public void setWidgetId(String widgetId)
		{
			this.widgetId = widgetId;
		}

		public String getDataSetFactoryId()
		{
			return dataSetFactoryId;
		}

		public void setDataSetFactoryId(String dataSetFactoryId)
		{
			this.dataSetFactoryId = dataSetFactoryId;
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		public static List<WidgetDataSetFactoryRelation> valuesOf(HtmlChartWidgetEntity obj)
		{
			List<WidgetDataSetFactoryRelation> list = new ArrayList<WidgetDataSetFactoryRelation>();

			if (obj == null)
				return list;

			SqlDataSetFactoryEntity[] dataSetFactoryEntities = obj.getSqlDataSetFactoryEntities();

			if (dataSetFactoryEntities == null)
				return list;

			for (int i = 0; i < dataSetFactoryEntities.length; i++)
			{
				WidgetDataSetFactoryRelation relation = new WidgetDataSetFactoryRelation(obj.getId(),
						dataSetFactoryEntities[i].getId(), i + 1);

				list.add(relation);
			}

			return list;
		}
	}
}
