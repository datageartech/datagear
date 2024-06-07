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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetField;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.domain.AnalysisProject;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.AuthorizationService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.PermissionDeniedException;
import org.datagear.management.service.UserService;
import org.datagear.management.util.dialect.MbSqlDialect;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
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

	private AnalysisProjectService analysisProjectService;

	private UserService userService;

	public HtmlChartWidgetEntityServiceImpl()
	{
		super();
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionFactory sqlSessionFactory, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			ChartPluginManager chartPluginManager, DataSetEntityService dataSetEntityService,
			AnalysisProjectService analysisProjectService,
			UserService userService)
	{
		super(sqlSessionFactory, dialect, authorizationService);
		this.chartPluginManager = chartPluginManager;
		this.dataSetEntityService = dataSetEntityService;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
	}

	public HtmlChartWidgetEntityServiceImpl(SqlSessionTemplate sqlSessionTemplate, MbSqlDialect dialect,
			AuthorizationService authorizationService,
			ChartPluginManager chartPluginManager, DataSetEntityService dataSetEntityService,
			AnalysisProjectService analysisProjectService,
			UserService userService)
	{
		super(sqlSessionTemplate, dialect, authorizationService);
		this.chartPluginManager = chartPluginManager;
		this.dataSetEntityService = dataSetEntityService;
		this.analysisProjectService = analysisProjectService;
		this.userService = userService;
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
	public ChartWidget getChartWidget(String id) throws Throwable
	{
		ChartWidgetSourceContext context = ChartWidgetSourceContext.get();

		HtmlChartWidgetEntity entity = null;

		if (context.hasUser())
			entity = super.getById(context.getUser(), id);
		else
			entity = super.getById(id);

		inflateHtmlChartWidgetEntity(entity, true);

		return entity;
	}

	@Override
	public HtmlChartWidgetEntity getById(User user, String id) throws PermissionDeniedException
	{
		HtmlChartWidgetEntity entity = super.getById(user, id);
		inflateHtmlChartWidgetEntity(entity, false);

		return entity;
	}

	@Override
	public HtmlChartWidgetEntity getByIdForEdit(User user, String id) throws PermissionDeniedException
	{
		HtmlChartWidgetEntity entity = super.getByIdForEdit(user, id);
		inflateHtmlChartWidgetEntity(entity, false);

		return entity;
	}

	@Override
	public HtmlChartWidgetEntity getById(String id)
	{
		HtmlChartWidgetEntity entity = super.getById(id);
		inflateHtmlChartWidgetEntity(entity, false);

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
	public PagingData<HtmlChartWidgetEntity> pagingQuery(User user, PagingQuery pagingQuery, String dataFilter,
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
	protected void add(HtmlChartWidgetEntity entity, Map<String, Object> params)
	{
		super.add(entity, params);
		saveWidgetDataSetRelations(entity);
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
	protected HtmlChartWidgetEntity getByIdFromDB(String id, Map<String, Object> params)
	{
		HtmlChartWidgetEntity entity = super.getByIdFromDB(id, params);
		setDataSetBindVOs(entity);

		return entity;
	}

	@Override
	protected HtmlChartWidgetEntity postProcessGet(HtmlChartWidgetEntity obj)
	{
		inflateAnalysisProjectAwareEntity(obj, this.analysisProjectService);
		inflateCreateUserEntity(obj, this.userService);

		return super.postProcessGet(obj);
	}

	protected void saveWidgetDataSetRelations(HtmlChartWidgetEntity entity)
	{
		if (entity == null)
			return;

		Map<String, Object> delParams = buildParamMap();
		delParams.put("id", entity.getId());

		deleteMybatis("deleteDataSetRelationById", delParams);

		List<WidgetDataSetRelation> relations = getWidgetDataSetRelations(entity);

		if (!relations.isEmpty())
		{
			for (WidgetDataSetRelation relation : relations)
			{
				Map<String, Object> insertParams = buildParamMap();
				insertParams.put("entity", relation);

				insertMybatis("insertDataSetRelation", insertParams);
			}
		}
	}

	@Override
	protected void postProcessQuery(List<HtmlChartWidgetEntity> list)
	{
		// 查询操作仅用于展示，不必完全加载
		for (HtmlChartWidgetEntity e : list)
			inflateHtmlChartPlugin(e, false);
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

	protected void setDataSetBindVOs(HtmlChartWidgetEntity entity)
	{
		if (entity == null)
			return;

		Map<String, Object> sqlParams = buildParamMap();
		sqlParams.put("widgetId", entity.getId());

		List<WidgetDataSetRelation> relations = selectListMybatis("getDataSetRelations", sqlParams);

		List<DataSetBindVO> dataSetBinds = new ArrayList<>(relations.size());

		for (int i = 0; i < relations.size(); i++)
		{
			DataSetBindVO dataSetBind = toDataSetBindVO(relations.get(i));

			if (dataSetBind != null)
				dataSetBinds.add(dataSetBind);
		}

		entity.setDataSetBinds(dataSetBinds.toArray(new DataSetBindVO[dataSetBinds.size()]));
	}

	protected DataSetBindVO toDataSetBindVO(WidgetDataSetRelation relation)
	{
		if (relation == null || StringUtil.isEmpty(relation.getDataSetId()))
			return null;

		IdDataSet dataSet = new IdDataSet(relation.getDataSetId());

		DataSetBindVO dataSetBind = new DataSetBindVO(dataSet);
		dataSetBind.setFieldSigns(toFieldSigns(relation.getFieldSignsJson()));
		dataSetBind.setAlias(relation.getAlias());
		dataSetBind.setAttachment(relation.isAttachment());
		dataSetBind.setQuery(toDataSetQuery(relation.getQueryJson()));
		dataSetBind.setFieldAliases(toFieldAliases(relation.getFieldAliasesJson()));
		dataSetBind.setFieldOrders(toFieldOrders(relation.getFieldOrdersJson()));

		return dataSetBind;
	}

	protected void inflateHtmlChartWidgetEntity(HtmlChartWidgetEntity entity, boolean forAnalysis)
	{
		if (entity == null)
			return;

		inflateHtmlChartPlugin(entity, forAnalysis);
		inflateDataSetBinds(entity, forAnalysis);
	}

	protected void inflateHtmlChartPlugin(HtmlChartWidgetEntity entity, boolean forAnalysis)
	{
		if (entity == null)
			return;

		HtmlChartPluginVo pluginVo = entity.getPluginVo();

		if (pluginVo != null)
		{
			HtmlChartPlugin full = getHtmlChartPlugin(pluginVo.getId());

			if (forAnalysis)
				entity.setPlugin(full);
			else
			{
				if (full != null)
				{
					pluginVo.setId(full.getId());
					pluginVo.setNameLabel(full.getNameLabel());
					pluginVo.setDescLabel(full.getDescLabel());
					pluginVo.setIconResourceNames(full.getIconResourceNames());
				}
			}
		}
	}

	protected void inflateDataSetBinds(HtmlChartWidgetEntity entity, boolean forAnalysis)
	{
		if (entity == null)
			return;

		DataSetBindVO[] dataSetBindVOs = entity.getDataSetBindVOs();

		if (dataSetBindVOs == null || dataSetBindVOs.length == 0)
			return;

		List<DataSetBindVO> list = new ArrayList<DataSetBindVO>(dataSetBindVOs.length);

		for (int i = 0; i < dataSetBindVOs.length; i++)
		{
			DataSetBindVO vo = dataSetBindVOs[i].clone();
			String dataSetId = vo.getDataSet().getId();

			DataSet dataSet = null;

			if (forAnalysis)
				dataSet = this.dataSetEntityService.getDataSet(dataSetId);
			else
				dataSet = this.dataSetEntityService.getById(dataSetId);

			vo.setDataSet(dataSet);

			addIfNonNull(list, (vo.getDataSet() == null ? null : vo));
		}
		
		entity.setDataSetBindVOs(list.toArray(new DataSetBindVO[list.size()]));
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Set<String>> toFieldSigns(String json)
	{
		if (StringUtil.isEmpty(json))
			return Collections.EMPTY_MAP;

		Map<String, Set<String>> fieldSigns = new HashMap<>();

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

			fieldSigns.put(entry.getKey(), signs);
		}

		return fieldSigns;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> toFieldAliases(String json)
	{
		if (StringUtil.isEmpty(json))
			return Collections.EMPTY_MAP;

		Map<String, String> aliases = JsonSupport.parse(json, Map.class, null);

		if (aliases == null)
			aliases = Collections.EMPTY_MAP;

		return aliases;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Number> toFieldOrders(String json)
	{
		if (StringUtil.isEmpty(json))
			return Collections.EMPTY_MAP;

		Map<String, Number> orders = JsonSupport.parse(json, Map.class, null);

		if (orders == null)
			orders = Collections.EMPTY_MAP;

		return orders;
	}

	protected DataSetQuery toDataSetQuery(String json)
	{
		DataSetQuery query = null;
		
		if (!StringUtil.isEmpty(json))
			query = JsonSupport.parse(json, DataSetQuery.class, null);
		
		if (query == null)
			query = DataSetQuery.valueOf();

		return query;
	}

	protected HtmlChartPlugin getHtmlChartPlugin(String id)
	{
		return (HtmlChartPlugin) this.chartPluginManager.get(id);
	}

	protected List<WidgetDataSetRelation> getWidgetDataSetRelations(HtmlChartWidgetEntity entity)
	{
		List<WidgetDataSetRelation> list = new ArrayList<>();

		if (entity == null)
			return list;

		DataSetBind[] dataSetBinds = entity.getDataSetBinds();

		if (dataSetBinds == null)
			return list;

		for (int i = 0; i < dataSetBinds.length; i++)
		{
			DataSetBind dataSetBind = dataSetBinds[i];

			String fieldSignsJson = JsonSupport.generate(dataSetBind.getFieldSigns(), "");
			String queryJson = JsonSupport.generate(dataSetBind.getQuery(), "");
			String fieldAliasesJson = JsonSupport.generate(dataSetBind.getFieldAliases(), "");
			String fieldOrdersJson = JsonSupport.generate(dataSetBind.getFieldOrders(), "");

			WidgetDataSetRelation relation = new WidgetDataSetRelation(entity.getId(),
					dataSetBind.getDataSet().getId(),
					i + 1);
			relation.setFieldSignsJson(fieldSignsJson);
			relation.setAlias(dataSetBind.getAlias());
			relation.setAttachment(dataSetBind.isAttachment());
			relation.setQueryJson(queryJson);
			relation.setFieldAliasesJson(fieldAliasesJson);
			relation.setFieldOrdersJson(fieldOrdersJson);

			list.add(relation);
		}

		return list;
	}

	/**
	 * {@linkplain DataSetBind}持久化值类型。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class WidgetDataSetRelation
	{
		private String widgetId;

		private String dataSetId;

		private String fieldSignsJson;

		private String alias;

		private boolean attachment;

		private String queryJson;

		private String fieldAliasesJson;

		private String fieldOrdersJson;

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

		public String getFieldSignsJson()
		{
			return fieldSignsJson;
		}

		public void setFieldSignsJson(String fieldSignsJson)
		{
			this.fieldSignsJson = fieldSignsJson;
		}

		public String getAlias()
		{
			return alias;
		}

		public void setAlias(String alias)
		{
			this.alias = alias;
		}

		public boolean isAttachment()
		{
			return attachment;
		}

		public void setAttachment(boolean attachment)
		{
			this.attachment = attachment;
		}

		public String getQueryJson()
		{
			return queryJson;
		}

		public void setQueryJson(String queryJson)
		{
			this.queryJson = queryJson;
		}

		public String getFieldAliasesJson()
		{
			return fieldAliasesJson;
		}

		public void setFieldAliasesJson(String fieldAliasesJson)
		{
			this.fieldAliasesJson = fieldAliasesJson;
		}

		public String getFieldOrdersJson()
		{
			return fieldOrdersJson;
		}

		public void setFieldOrdersJson(String fieldOrdersJson)
		{
			this.fieldOrdersJson = fieldOrdersJson;
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

	protected static class IdDataSet extends AbstractIdentifiable implements DataSet, Serializable
	{
		private static final long serialVersionUID = 1L;

		public IdDataSet()
		{
			super();
		}

		public IdDataSet(String id)
		{
			super(id);
		}

		@Override
		public String getName()
		{
			return "";
		}

		@Override
		public boolean isMutableModel()
		{
			return false;
		}

		@Override
		public List<DataSetField> getFields()
		{
			return Collections.emptyList();
		}

		@Override
		public DataSetField getField(String name)
		{
			return null;
		}

		@Override
		public List<DataSetParam> getParams()
		{
			return Collections.emptyList();
		}

		@Override
		public DataSetParam getParam(String name)
		{
			return null;
		}

		@Override
		public DataSetResult getResult(DataSetQuery query) throws DataSetException
		{
			return null;
		}
	}
}
