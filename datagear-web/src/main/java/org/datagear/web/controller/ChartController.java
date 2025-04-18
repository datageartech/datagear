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

package org.datagear.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.support.ChartPluginAttributeValueConverter;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.domain.AnalysisProjectAwareEntity;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.UserService;
import org.datagear.management.util.ManagementSupport;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.util.function.OnceSupplier;
import org.datagear.web.util.AnalysisProjectAwareSupport;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 图表控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/chart")
public class ChartController extends AbstractChartPluginAwareController
{
	@Autowired
	private HtmlChartWidgetEntityService htmlChartWidgetEntityService;

	@Autowired
	private AnalysisProjectService analysisProjectService;

	@Autowired
	private ChartPluginManager chartPluginManager;

	@Autowired
	private DataSetEntityService dataSetEntityService;

	@Autowired
	private UserService userService;

	@Autowired
	private ManagementSupport managementSupport;

	@Autowired
	private AnalysisProjectAwareSupport analysisProjectAwareSupport;

	private ChartPluginAttributeValueConverter chartPluginAttributeValueConverter = new ChartPluginAttributeValueConverter();

	public ChartController()
	{
		super();
	}

	public HtmlChartWidgetEntityService getHtmlChartWidgetEntityService()
	{
		return htmlChartWidgetEntityService;
	}

	public void setHtmlChartWidgetEntityService(HtmlChartWidgetEntityService htmlChartWidgetEntityService)
	{
		this.htmlChartWidgetEntityService = htmlChartWidgetEntityService;
	}

	public AnalysisProjectService getAnalysisProjectService()
	{
		return analysisProjectService;
	}

	public void setAnalysisProjectService(AnalysisProjectService analysisProjectService)
	{
		this.analysisProjectService = analysisProjectService;
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

	public UserService getUserService()
	{
		return userService;
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}

	public ManagementSupport getManagementSupport()
	{
		return managementSupport;
	}

	public void setManagementSupport(ManagementSupport managementSupport)
	{
		this.managementSupport = managementSupport;
	}

	public AnalysisProjectAwareSupport getAnalysisProjectAwareSupport()
	{
		return analysisProjectAwareSupport;
	}

	public void setAnalysisProjectAwareSupport(AnalysisProjectAwareSupport analysisProjectAwareSupport)
	{
		this.analysisProjectAwareSupport = analysisProjectAwareSupport;
	}

	public ChartPluginAttributeValueConverter getChartPluginAttributeValueConverter()
	{
		return chartPluginAttributeValueConverter;
	}

	public void setChartPluginAttributeValueConverter(
			ChartPluginAttributeValueConverter chartPluginAttributeValueConverter)
	{
		this.chartPluginAttributeValueConverter = chartPluginAttributeValueConverter;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		setFormAction(model, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE_ADD);

		HtmlChartWidgetEntity entity = createAdd(request, model);
		setRequestAnalysisProject(request, entity);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);

		return "/chart/chart_form";
	}

	protected HtmlChartWidgetEntity createAdd(HttpServletRequest request, Model model)
	{
		return createInstance();
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlChartWidgetEntity entity)
	{
		User user = getCurrentUser();

		entity.setId(IDUtil.randomIdOnTime20());
		inflateCreateUserAndTime(entity, user);
		inflateSaveEntity(request, user, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity, null);

		if (re != null)
			return re;

		this.htmlChartWidgetEntityService.add(user, entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE_EDIT);
		
		HtmlChartWidgetEntity entity = getByIdForEdit(this.htmlChartWidgetEntityService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlChartWidgetEntity entity)
	{
		User user = getCurrentUser();

		inflateSaveEntity(request, user, entity);

		ResponseEntity<OperationMessage> re = checkSaveEntity(request, user, entity,
				new OnceSupplier<>(() ->
				{
					return getByIdForEdit(getHtmlChartWidgetEntityService(), user, entity.getId());
				}));

		if (re != null)
			return re;

		this.htmlChartWidgetEntityService.update(user, entity);

		toFormResponseData(request, entity);

		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id) throws Exception
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE_ADD);

		// 统一复制规则，至少有编辑权限才允许复制
		HtmlChartWidgetEntity entity = getByIdForEdit(this.htmlChartWidgetEntityService, user, id);
		toCopyResponseData(request, user, entity);
		setFormPageAttr(request, model, entity);

		return "/chart/chart_form";
	}

	protected void toCopyResponseData(HttpServletRequest request, User user, HtmlChartWidgetEntity entity)
			throws Exception
	{
		this.analysisProjectAwareSupport.setRefNullIfDenied(user, entity, getAnalysisProjectService());

		DataSetBind[] dataSetBinds = entity.getDataSetBinds();
		if (dataSetBinds != null)
		{
			List<DataSetBind> dataSetBindsPermited = new ArrayList<DataSetBind>(dataSetBinds.length);

			for (int i = 0; i < dataSetBinds.length; i++)
			{
				DataSetBind dataSetBind = dataSetBinds[i];
				
				if (dataSetBind == null)
					continue;

				this.managementSupport.setRefNullIfDenied(user, dataSetBind, (t) ->
				{
					return (DataSetEntity) t.getDataSet();

				}, (t) ->
				{
					t.setDataSet(null);

				}, getDataSetEntityService());
				
				if (dataSetBind.getDataSet() != null)
				{
					dataSetBindsPermited.add(dataSetBind);
				}
			}

			dataSetBinds = dataSetBindsPermited.toArray(new DataSetBind[dataSetBindsPermited.size()]);
			entity.setDataSetBinds(dataSetBinds);
		}

		toFormResponseData(request, entity);
		entity.setId(null);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		setFormAction(model, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);

		HtmlChartWidgetEntity entity = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		toFormResponseData(request, entity);
		setFormPageAttr(request, model, entity);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestBody String[] ids)
	{
		User user = getCurrentUser();

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlChartWidgetEntityService.deleteById(user, id);
		}

		return optSuccessResponseEntity(request);
	}

	@RequestMapping("/manage")
	public String manage(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_MANAGE);
		setReadonlyAction(model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartWidgetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			Model model, @RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam)
			throws Exception
	{
		User user = getCurrentUser();
		APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlChartWidgetEntity> pagingData = this.htmlChartWidgetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());
		toQueryResponseData(request, pagingData.getItems());

		return pagingData;
	}

	@RequestMapping(value = "/hasReadPermission", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public boolean[] hasReadPermission(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HasReadPermissionForm form) throws Exception
	{
		if(StringUtil.isEmpty(form.getUserId()))
			throw new IllegalInputException();
		
		String[] chartWidgetIds = form.getChartWidgetIds();
		
		if(isEmpty(chartWidgetIds))
			return new boolean[0];
		
		User user  = this.userService.getByIdSimple(form.getUserId());
		
		if(user == null)
			throw new IllegalInputException();
		
		boolean[] re = new boolean[chartWidgetIds.length];
		
		int[] permissions = getHtmlChartWidgetEntityService().getPermissions(user, chartWidgetIds);
		
		for(int i=0; i<chartWidgetIds.length; i++)
		{
			re[i] = Authorization.canRead(permissions[i]);
		}
		
		return re;
	}

	protected ResponseEntity<OperationMessage> checkSaveEntity(HttpServletRequest request, User user,
			HtmlChartWidgetEntity entity, OnceSupplier<HtmlChartWidgetEntity> persist)
	{
		if (isEmpty(entity.getId()) || isBlank(entity.getName()))
			throw new IllegalInputException();

		if (isEmpty(entity.getPluginVo()))
			throw new IllegalInputException();

		checkSaveRefAnalysisProject(request, user, entity, persist);

		DataSetBind[] dsbs = entity.getDataSetBinds();
		DataSetBind[] persistDsbs = (persist == null ? null : persist.get().getDataSetBinds());

		if (dsbs != null && dsbs.length > 0)
		{
			for (int i = 0; i < dsbs.length; i++)
			{
				DataSetBind dsb = dsbs[i];
				DataSetBind persistDsb = (persistDsbs == null || i >= persistDsbs.length ? null : persistDsbs[i]);

				this.managementSupport.checkSaveRef(user, dsb, persistDsb, (t) ->
				{
					return (DataSetEntity) t.getDataSet();

				}, (r) ->
				{
					return r.getName();

				}, getDataSetEntityService());
			}
		}

		return null;
	}

	protected void trimAnalysisProjectAware(AnalysisProjectAwareEntity entity)
	{
		this.analysisProjectAwareSupport.trim(entity);
	}

	@SuppressWarnings("unchecked")
	protected void checkSaveRefAnalysisProject(HttpServletRequest request, User user,
			AnalysisProjectAwareEntity dataSet, Supplier<? extends AnalysisProjectAwareEntity> persist)
	{
		this.analysisProjectAwareSupport.checkSaveSupplier(user, dataSet,
				(Supplier<AnalysisProjectAwareEntity>) persist, getAnalysisProjectService());
	}

	protected void setFormPageAttr(HttpServletRequest request, Model model, HtmlChartWidgetEntity entity)
	{
		setFormModel(model, entity);
		setResultDataFormatModel(request, model, entity);
		setDisableSaveShowAttr(request, model);
	}

	protected void inflateSaveEntity(HttpServletRequest request, User user, HtmlChartWidgetEntity entity)
	{
		trimAnalysisProjectAware(entity);

		// 如果插件不存在，应置为null
		HtmlChartPluginVo pluginVo = entity.getPluginVo();
		String pluginId = (pluginVo == null ? null : pluginVo.getId());
		HtmlChartPlugin plugin = null;
		if (!StringUtil.isEmpty(pluginId))
		{
			plugin = (HtmlChartPlugin) this.chartPluginManager.get(pluginId);
			pluginVo = (plugin == null ? null : new HtmlChartPluginVo(plugin.getId(), plugin.getNameLabel()));
			entity.setPluginVo(pluginVo);
		}

		DataSetBindVO[] dataSetBindVOs = entity.getDataSetBindVOs();
		if (dataSetBindVOs != null)
		{
			for (DataSetBindVO vo : dataSetBindVOs)
			{
				DataSetQuery query = vo.getQuery();
				query = getWebDashboardQueryConverter().convert(query, vo.getDataSet());
				vo.setQuery(query);
			}
		}

		Map<String, Object> attrValues = entity.getAttrValues();
		if (attrValues != null && plugin != null)
		{
			attrValues = getChartPluginAttributeValueConverter().convert(attrValues, plugin.getAttributes());
			entity.setAttrValues(attrValues);
		}
	}

	protected void toFormResponseData(HttpServletRequest request, HtmlChartWidgetEntity entity)
	{
		HtmlChartPlugin plugin = entity.getPluginVo();

		if (plugin != null)
			entity.setPluginVo(getHtmlChartPluginView(request, plugin.getId()));

		entity.setDataSetBinds(toDataSetBindViews(entity.getDataSetBinds()));
	}

	protected void toQueryResponseData(HttpServletRequest request, List<HtmlChartWidgetEntity> items)
	{
		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		for (HtmlChartWidgetEntity entity : items)
		{
			entity.setPluginVo(toHtmlChartPluginView(entity.getPluginVo(), themeName, locale));
		}
	}

	protected void setRequestAnalysisProject(HttpServletRequest request, HtmlChartWidgetEntity entity)
	{
		setRequestAnalysisProjectIfValid(request, this.analysisProjectService, entity);
	}

	protected boolean setDisableSaveShowAttr(HttpServletRequest request, Model model)
	{
		boolean disable = isDisableSaveShow(request, model);
		model.addAttribute("disableSaveShow", disable);

		return disable;
	}

	/**
	 * 是否在图表表单页面禁用【保存并展示】功能按钮。
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	protected boolean isDisableSaveShow(HttpServletRequest request, Model model)
	{
		String pv = request.getParameter("disableSaveShow");
		return ("1".equals(pv) || "true".equals(pv));
	}
	
	protected void setResultDataFormatModel(HttpServletRequest request, Model model, HtmlChartWidgetEntity entity)
	{
		addAttributeForWriteJson(model, "initResultDataFormat",
				(entity.getResultDataFormat() != null ? entity.getResultDataFormat() : createResultDataFormat()));
		model.addAttribute("enableResultDataFormat", (entity.getResultDataFormat() != null));
	}

	protected HtmlChartWidgetEntity createInstance()
	{
		return new HtmlChartWidgetEntity();
	}

	protected ResultDataFormat createResultDataFormat()
	{
		return new ResultDataFormat();
	}
	
	public static class HasReadPermissionForm implements ControllerForm
	{
		private static final long serialVersionUID = 1L;
		
		private String userId;
		private String[] chartWidgetIds;
		
		public HasReadPermissionForm()
		{
			super();
		}

		public String getUserId()
		{
			return userId;
		}

		public void setUserId(String userId)
		{
			this.userId = userId;
		}

		public String[] getChartWidgetIds()
		{
			return chartWidgetIds;
		}

		public void setChartWidgetIds(String[] chartWidgetIds)
		{
			this.chartWidgetIds = chartWidgetIds;
		}
	}
}
