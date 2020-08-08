/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.domain.ChartDataSetVO;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.util.StringUtil;
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

	private DataSetEntityService dataSetEntityService;

	private AuthorizationService authorizationService;

	public HtmlChartWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, ChartPluginManager chartPluginManager,
			DataSetEntityService dataSetEntityService, AuthorizationService authorizationService)
	{
		super(sqlSessionFactory);
		this.chartPluginManager = chartPluginManager;
		this.dataSetEntityService = dataSetEntityService;
		this.authorizationService = authorizationService;
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate,
			ChartPluginManager chartPluginManager, DataSetEntityService dataSetEntityService,
			AuthorizationService authorizationService)
	{
		super(sqlSessionTemplate);
		this.chartPluginManager = chartPluginManager;
		this.dataSetEntityService = dataSetEntityService;
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

	public DataSetEntityService getDataSetEntityService()
	{
		return dataSetEntityService;
	}

	public void setDataSetEntityService(DataSetEntityService dataSetEntityService)
	{
		this.dataSetEntityService = dataSetEntityService;
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
	public ChartWidget getChartWidget(String id) throws Throwable
	{
		ChartWidgetSourceContext context = ChartWidgetSourceContext.get();

		HtmlChartWidgetEntity entity = null;

		if (context.hasUser())
			entity = getById(context.getUser(), id);
		else
			entity = getById(id);

		if (entity == null)
			return null;

		setHtmlChartPlugin(entity, true);
		setChartDataSets(entity, true);

		return entity;
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
			saveWidgetDataSetRelations(entity);

		return success;
	}

	@Override
	protected boolean update(HtmlChartWidgetEntity entity, Map<String, Object> params)
	{
		boolean success = super.update(entity, params);

		if (success)
			saveWidgetDataSetRelations(entity);

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

	protected void saveWidgetDataSetRelations(HtmlChartWidgetEntity entity)
	{
		deleteMybatis("deleteDataSetRelationById", entity.getId());

		List<WidgetDataSetRelation> relations = getWidgetDataSetRelations(entity);

		if (!relations.isEmpty())
		{
			for (WidgetDataSetRelation relation : relations)
				insertMybatis("insertDataSetRelation", relation);
		}
	}

	@Override
	protected void postProcessSelects(List<HtmlChartWidgetEntity> list)
	{
		// 查询操作仅用于展示，不必完全加载
		if (list == null)
			return;

		for (HtmlChartWidgetEntity e : list)
			setHtmlChartPlugin(e, false);
	}

	@Override
	protected HtmlChartWidgetEntity postProcessSelect(HtmlChartWidgetEntity obj)
	{
		if (obj == null)
			return null;

		setHtmlChartPlugin(obj, false);
		setChartDataSets(obj, false);

		return obj;
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

	protected void setHtmlChartPlugin(HtmlChartWidgetEntity obj, boolean forAnalysis)
	{
		HtmlChartPlugin htmlChartPlugin = obj.getHtmlChartPlugin();

		if (htmlChartPlugin != null)
		{
			HtmlChartPlugin full = getHtmlChartPlugin(htmlChartPlugin.getId());

			if (forAnalysis)
				obj.setHtmlChartPlugin(full);
			else
			{
				if (full != null)
				{
					htmlChartPlugin.setNameLabel(full.getNameLabel());
					htmlChartPlugin.setDescLabel(full.getDescLabel());
				}
			}
		}
	}

	protected void setChartDataSets(HtmlChartWidgetEntity widget, boolean forAnalysis)
	{
		Map<String, Object> sqlParams = buildParamMapWithIdentifierQuoteParameter();
		sqlParams.put("widgetId", widget.getId());

		List<WidgetDataSetRelation> relations = selectListMybatis("getDataSetRelations", sqlParams);

		List<ChartDataSetVO> chartDataSets = new ArrayList<>(relations.size());

		for (int i = 0; i < relations.size(); i++)
		{
			ChartDataSetVO chartDataSet = toChartDataSet(relations.get(i), forAnalysis);

			if (chartDataSet != null)
				chartDataSets.add(chartDataSet);
		}

		widget.setChartDataSets(chartDataSets.toArray(new ChartDataSetVO[chartDataSets.size()]));
	}

	protected ChartDataSetVO toChartDataSet(WidgetDataSetRelation relation, boolean forAnalysis)
	{
		if (relation == null || StringUtil.isEmpty(relation.getDataSetId()))
			return null;

		DataSet dataSet = null;

		if (forAnalysis)
			dataSet = this.dataSetEntityService.getDataSet(relation.getDataSetId());
		else
			dataSet = this.dataSetEntityService.getById(relation.getDataSetId());

		if (dataSet == null)
			return null;

		ChartDataSetVO chartDataSet = new ChartDataSetVO(dataSet);
		chartDataSet.setPropertySigns(toPropertySigns(relation.getPropertySignsJson()));
		chartDataSet.setAlias(relation.getAlias());
		chartDataSet.setParamValues(toParamValues(relation.getParamValuesJson()));

		return chartDataSet;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Set<String>> toPropertySigns(String json)
	{
		if (StringUtil.isEmpty(json))
			return Collections.EMPTY_MAP;

		Map<String, Set<String>> propertySigns = new HashMap<>();

		Map<String, Object> jsonMap = JsonSupport.parse(json, Map.class, null);
		if (jsonMap == null)
			jsonMap = Collections.EMPTY_MAP;

		for (Map.Entry<String, Object> entry : jsonMap.entrySet())
		{
			Set<String> signs = new HashSet<>();

			Object valueObj = entry.getValue();

			if (valueObj instanceof String)
				signs.add((String) valueObj);
			else if (valueObj instanceof Collection<?>)
			{
				Collection<String> valueCollection = (Collection<String>) valueObj;
				signs.addAll(valueCollection);
			}
			else if (valueObj instanceof Object[])
			{
				Object[] valueArray = (Object[]) valueObj;
				for (Object value : valueArray)
				{
					if (value instanceof String)
						signs.add((String) value);
				}
			}

			propertySigns.put(entry.getKey(), signs);
		}

		return propertySigns;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> toParamValues(String json)
	{
		if (StringUtil.isEmpty(json))
			return Collections.EMPTY_MAP;

		Map<String, Object> paramValues = JsonSupport.parse(json, Map.class, null);
		if (paramValues == null)
			paramValues = Collections.EMPTY_MAP;

		return paramValues;
	}

	protected HtmlChartPlugin getHtmlChartPlugin(String id)
	{
		return (HtmlChartPlugin) this.chartPluginManager.get(id);
	}

	protected List<WidgetDataSetRelation> getWidgetDataSetRelations(HtmlChartWidgetEntity obj)
	{
		List<WidgetDataSetRelation> list = new ArrayList<>();

		if (obj == null)
			return list;

		ChartDataSet[] chartDataSets = obj.getChartDataSets();

		if (chartDataSets == null)
			return list;

		for (int i = 0; i < chartDataSets.length; i++)
		{
			ChartDataSet chartDataSet = chartDataSets[i];

			String propertySignsJson = JsonSupport.generate(chartDataSet.getPropertySigns(), "");
			String paramValuesJson = JsonSupport.generate(chartDataSet.getParamValues(), "");

			WidgetDataSetRelation relation = new WidgetDataSetRelation(obj.getId(), chartDataSet.getDataSet().getId(),
					i + 1);
			relation.setPropertySignsJson(propertySignsJson);
			relation.setAlias(chartDataSet.getAlias());
			relation.setParamValuesJson(paramValuesJson);

			list.add(relation);
		}

		return list;
	}

	public static class WidgetDataSetRelation
	{
		private String widgetId;

		private String dataSetId;

		private String propertySignsJson;

		private String alias;

		private String paramValuesJson;

		private int order;

		public WidgetDataSetRelation()
		{
			super();
		}

		public WidgetDataSetRelation(String widgetId, String dataSetId, int order)
		{
			super();
			this.widgetId = widgetId;
			this.dataSetId = dataSetId;
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

		public String getDataSetId()
		{
			return dataSetId;
		}

		public void setDataSetId(String dataSetId)
		{
			this.dataSetId = dataSetId;
		}

		public String getPropertySignsJson()
		{
			return propertySignsJson;
		}

		public void setPropertySignsJson(String propertySignsJson)
		{
			this.propertySignsJson = propertySignsJson;
		}

		public String getAlias()
		{
			return alias;
		}

		public void setAlias(String alias)
		{
			this.alias = alias;
		}

		public String getParamValuesJson()
		{
			return paramValuesJson;
		}

		public void setParamValuesJson(String paramValuesJson)
		{
			this.paramValuesJson = paramValuesJson;
		}

		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}
	}
}
