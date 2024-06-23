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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetBind;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.ResultDataFormat;
import org.datagear.analysis.support.ChartPluginAttributeValueConverter;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.management.domain.Authorization;
import org.datagear.management.domain.DataSetBindVO;
import org.datagear.management.domain.HtmlChartPluginVo;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.AnalysisProjectService;
import org.datagear.management.service.DataSetEntityService;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.management.service.UserService;
import org.datagear.persistence.PagingData;
import org.datagear.util.IDUtil;
import org.datagear.util.StringUtil;
import org.datagear.web.util.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.datagear.web.vo.APIDDataFilterPagingQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
	public String add(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		HtmlChartWidgetEntity chart = new HtmlChartWidgetEntity();
		setRequestAnalysisProject(request, response, chart);

		addAttributeForWriteJson(model, "chartPluginVO", null);
		model.addAttribute("initResultDataFormat", createDefaultResultDataFormat());
		model.addAttribute("enableResultDataFormat", false);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_ADD, SUBMIT_ACTION_SAVE);

		return "/chart/chart_form";
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();
		
		HtmlChartWidgetEntity chart = getByIdForEdit(this.htmlChartWidgetEntityService, user, id);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_EDIT, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping("/copy")
	public String copy(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		HtmlChartWidgetEntity chart = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		setNullAnalysisProjectIfNoPermission(user, chart, getAnalysisProjectService());

		DataSetBind[] dataSetBinds = chart.getDataSetBinds();
		if (dataSetBinds != null)
		{
			List<DataSetBind> dataSetBindsPermited = new ArrayList<DataSetBind>(dataSetBinds.length);

			for (int i = 0; i < dataSetBinds.length; i++)
			{
				DataSetBind dataSetBind = dataSetBinds[i];
				DataSet dataSet = (dataSetBind == null ? null : dataSetBind.getDataSet());
				int permission = (dataSet != null ? getDataSetEntityService().getPermission(user, dataSet.getId())
						: Authorization.PERMISSION_NONE_START);

				// 只添加有权限的
				if (Authorization.canRead(permission))
				{
					dataSetBindsPermited.add(dataSetBind);
				}
			}

			dataSetBinds = dataSetBindsPermited.toArray(new DataSetBind[dataSetBindsPermited.size()]);
		}

		chart.setId(null);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);
		setDisableSaveShowAttr(request, response, model);

		setFormModel(model, chart, REQUEST_ACTION_COPY, SUBMIT_ACTION_SAVE);
		
		return "/chart/chart_form";
	}

	@RequestMapping(value = "/save", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody HtmlChartWidgetEntity entity)
	{
		User user = getCurrentUser();

		trimAnalysisProjectAwareEntityForSave(entity);

		HtmlChartPluginVo paramPlugin = entity.getPluginVo();

		if (isEmpty(entity.getId()))
		{
			entity.setId(IDUtil.randomIdOnTime20());
			inflateCreateUserAndTime(entity, user);
			inflateHtmlChartWidgetEntity(entity, request);

			checkSaveEntity(entity);

			this.htmlChartWidgetEntityService.add(user, entity);
		}
		else
		{
			inflateHtmlChartWidgetEntity(entity, request);
			checkSaveEntity(entity);
			this.htmlChartWidgetEntityService.update(user, entity);
		}

		// 返回参数不应该完全加载插件对象
		entity.setPluginVo(paramPlugin);
		
		return optSuccessDataResponseEntity(request, entity);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = getCurrentUser();

		HtmlChartWidgetEntity chart = getByIdForView(this.htmlChartWidgetEntityService, user, id);
		convertForFormModel(chart, request);
		setResultDataFormatModel(chart, model);

		setFormModel(model, chart, REQUEST_ACTION_VIEW, SUBMIT_ACTION_NONE);
		
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

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		model.addAttribute(KEY_REQUEST_ACTION, REQUEST_ACTION_QUERY);
		setReadonlyAction(model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute("serverURL", WebUtils.getServerURL(request));
		setSelectAction(request, model);
		addAttributeForWriteJson(model, KEY_CURRENT_ANALYSIS_PROJECT,
				getRequestAnalysisProject(request, response, getAnalysisProjectService()));
		
		return "/chart/chart_table";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartWidgetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel,
			@RequestBody(required = false) APIDDataFilterPagingQuery pagingQueryParam) throws Exception
	{
		User user = getCurrentUser();
		final APIDDataFilterPagingQuery pagingQuery = inflateAPIDDataFilterPagingQuery(request, pagingQueryParam);

		PagingData<HtmlChartWidgetEntity> pagingData = this.htmlChartWidgetEntityService.pagingQuery(user, pagingQuery,
				pagingQuery.getDataFilter(), pagingQuery.getAnalysisProjectId());
		setChartPluginView(request, pagingData.getItems());

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

	protected void setChartPluginView(HttpServletRequest request, List<HtmlChartWidgetEntity> entities)
	{
		if (entities == null)
			return;

		Locale locale = WebUtils.getLocale(request);
		String themeName = resolveChartPluginIconThemeName(request);

		for (HtmlChartWidgetEntity entity : entities)
		{
			entity.setPluginVo(toHtmlChartPluginView(entity.getPluginVo(), themeName, locale));
		}
	}

	protected void inflateHtmlChartWidgetEntity(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		// 如果插件不存在，应置为null
		HtmlChartPluginVo pluginVo = entity.getPluginVo();
		String pluginId = (pluginVo == null ? null : pluginVo.getId());
		HtmlChartPlugin plugin = null;
		if (!StringUtil.isEmpty(pluginId))
		{
			plugin = (HtmlChartPlugin) this.chartPluginManager.get(pluginId);
			pluginVo = (plugin == null ? null
					: new HtmlChartPluginVo(plugin.getId(), plugin.getNameLabel()));
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

	protected void setRequestAnalysisProject(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity entity)
	{
		setRequestAnalysisProjectIfValid(request, response, this.analysisProjectService, entity);
	}

	protected boolean setDisableSaveShowAttr(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		boolean disable = isDisableSaveShow(request, response, model);
		model.addAttribute("disableSaveShow", disable);

		return disable;
	}

	/**
	 * 是否在图表表单页面禁用【保存并展示】功能按钮。
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	protected boolean isDisableSaveShow(HttpServletRequest request, HttpServletResponse response,
			org.springframework.ui.Model model)
	{
		String pv = request.getParameter("disableSaveShow");
		return ("1".equals(pv) || "true".equals(pv));
	}

	protected ResultDataFormat createDefaultResultDataFormat()
	{
		ResultDataFormat rdf = new ResultDataFormat();
		return rdf;
	}

	protected void checkSaveEntity(HtmlChartWidgetEntity chart)
	{
		if (isBlank(chart.getName()))
			throw new IllegalInputException();

		if (isEmpty(chart.getPluginVo()))
			throw new IllegalInputException();
	}
	
	protected void convertForFormModel(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		HtmlChartPlugin plugin = entity.getPluginVo();
		
		if(plugin != null)
			entity.setPluginVo(getHtmlChartPluginView(request, plugin.getId()));
		
		entity.setDataSetBinds(toDataSetBindViews(entity.getDataSetBinds()));
	}
	
	protected void setResultDataFormatModel(HtmlChartWidgetEntity entity, org.springframework.ui.Model model)
	{
		addAttributeForWriteJson(model, "initResultDataFormat",
				(entity.getResultDataFormat() != null ? entity.getResultDataFormat() : createDefaultResultDataFormat()));
		model.addAttribute("enableResultDataFormat", (entity.getResultDataFormat() != null));
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
