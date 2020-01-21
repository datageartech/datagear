/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.SqlDataSetEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/analysis/chart")
public class ChartController extends AbstractChartPluginAwareController
{
	@Autowired
	private HtmlChartWidgetEntityService htmlChartWidgetEntityService;

	@Autowired
	private ChartPluginManager chartPluginManager;

	public ChartController()
	{
		super();
	}

	public ChartController(HtmlChartWidgetEntityService htmlChartWidgetEntityService,
			ChartPluginManager chartPluginManager)
	{
		super();
		this.htmlChartWidgetEntityService = htmlChartWidgetEntityService;
		this.chartPluginManager = chartPluginManager;
	}

	public HtmlChartWidgetEntityService getHtmlChartWidgetEntityService()
	{
		return htmlChartWidgetEntityService;
	}

	public void setHtmlChartWidgetEntityService(HtmlChartWidgetEntityService htmlChartWidgetEntityService)
	{
		this.htmlChartWidgetEntityService = htmlChartWidgetEntityService;
	}

	public ChartPluginManager getChartPluginManager()
	{
		return chartPluginManager;
	}

	public void setChartPluginManager(ChartPluginManager chartPluginManager)
	{
		this.chartPluginManager = chartPluginManager;
	}

	@RequestMapping("/add")
	public String add(HttpServletRequest request, org.springframework.ui.Model model)
	{
		HtmlChartWidgetEntity chart = new HtmlChartWidgetEntity();

		List<HtmlChartPluginVO> pluginVOs = findHtmlChartPluginVOs(request, null);

		if (pluginVOs.size() > 0)
		{
			String defaultChartPluginId = pluginVOs.get(0).getId();
			chart.setHtmlChartPlugin((HtmlChartPlugin<HtmlRenderContext>) this.chartPluginManager
					.<HtmlRenderContext> get(defaultChartPluginId));
		}

		model.addAttribute("chart", chart);
		model.addAttribute("pluginVOs", toWriteJsonTemplateModel(pluginVOs));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.addChart");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity entity)
	{
		User user = WebUtils.getUser(request, response);

		entity.setId(IDUtil.uuid());
		entity.setCreateUser(user);
		inflateHtmlChartWidgetEntity(entity, request);

		checkSaveEntity(entity);

		this.htmlChartWidgetEntityService.add(user, entity);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getByIdForEdit(user, id);

		if (chart == null)
			throw new RecordNotFoundException();

		List<HtmlChartPluginVO> pluginVOs = findHtmlChartPluginVOs(request, null);

		model.addAttribute("chart", chart);
		model.addAttribute("chartDataSets", toWriteJsonTemplateModel(chart.getChartDataSets()));
		model.addAttribute("pluginVOs", toWriteJsonTemplateModel(pluginVOs));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.editChart");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity entity)
	{
		User user = WebUtils.getUser(request, response);

		inflateHtmlChartWidgetEntity(entity, request);

		checkSaveEntity(entity);

		this.htmlChartWidgetEntityService.update(user, entity);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		User user = WebUtils.getUser(request, response);

		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(user, id);

		if (chart == null)
			throw new RecordNotFoundException();

		List<HtmlChartPluginVO> pluginVOs = findHtmlChartPluginVOs(request, null);

		model.addAttribute("chart", chart);
		model.addAttribute("chartDataSets", toWriteJsonTemplateModel(chart.getChartDataSets()));
		model.addAttribute("pluginVOs", toWriteJsonTemplateModel(pluginVOs));
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.viewChart");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
	{
		User user = WebUtils.getUser(request, response);

		for (int i = 0; i < ids.length; i++)
		{
			String id = ids[i];
			this.htmlChartWidgetEntityService.deleteById(user, id);
		}

		return buildOperationMessageDeleteSuccessResponseEntity(request);
	}

	@RequestMapping("/pagingQuery")
	public String pagingQuery(HttpServletRequest request, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.manageChart");

		return "/analysis/chart/chart_grid";
	}

	@RequestMapping(value = "/select")
	public String select(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model)
	{
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.selectChart");
		model.addAttribute(KEY_SELECTONLY, true);

		return "/analysis/chart/chart_grid";
	}

	@RequestMapping(value = "/pagingQueryData", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public PagingData<HtmlChartWidgetEntity> pagingQueryData(HttpServletRequest request, HttpServletResponse response,
			final org.springframework.ui.Model springModel) throws Exception
	{
		User user = WebUtils.getUser(request, response);

		PagingQuery pagingQuery = getPagingQuery(request);

		PagingData<HtmlChartWidgetEntity> pagingData = this.htmlChartWidgetEntityService.pagingQuery(user, pagingQuery);
		setChartPluginNames(request, pagingData.getItems());

		return pagingData;
	}

	protected void setChartPluginNames(HttpServletRequest request, List<HtmlChartWidgetEntity> entities)
	{
		if (entities == null)
			return;

		Locale locale = WebUtils.getLocale(request);

		for (HtmlChartWidgetEntity entity : entities)
			entity.updateChartPluginName(locale);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void inflateHtmlChartWidgetEntity(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin = entity.getHtmlChartPlugin();

		if (htmlChartPlugin != null)
		{
			htmlChartPlugin = (HtmlChartPlugin<HtmlRenderContext>) (ChartPlugin) this.chartPluginManager
					.get(htmlChartPlugin.getId());

			entity.setHtmlChartPlugin(htmlChartPlugin);
		}

		inflateChartDataSets(entity, request);
	}

	protected void inflateChartDataSets(HtmlChartWidgetEntity entity, HttpServletRequest request)
	{
		String[] chartDataSetIndexes = request.getParameterValues("chartDataSetIndex");

		if (isEmpty(chartDataSetIndexes))
			return;

		List<ChartDataSet> chartDataSets = new ArrayList<ChartDataSet>();

		for (String chartDataSetIndex : chartDataSetIndexes)
		{
			String dataSetId = request.getParameter("chartDataSet_" + chartDataSetIndex + "_dataSetId");

			if (isEmpty(dataSetId))
				continue;

			SqlDataSetEntity sqlDataSet = new SqlDataSetEntity();
			sqlDataSet.setId(dataSetId);

			Map<String, Set<String>> propertySigns = new HashMap<String, Set<String>>();

			String[] propertySignIndexes = request
					.getParameterValues("chartDataSet_" + chartDataSetIndex + "_propertySignIndex");

			if (!isEmpty(propertySignIndexes))
			{
				for (String propertySignIndex : propertySignIndexes)
				{
					String propertyName = request.getParameter(
							"chartDataSet_" + chartDataSetIndex + "_propertySign_" + propertySignIndex + "_name");

					if (isEmpty(propertyName))
						continue;

					String[] signs = request.getParameterValues(
							"chartDataSet_" + chartDataSetIndex + "_propertySign_" + propertySignIndex + "_value");

					if (!isEmpty(signs))
					{
						Set<String> signSet = new HashSet<String>();
						for (String sign : signs)
							signSet.add(sign);

						propertySigns.put(propertyName, signSet);
					}
				}
			}

			ChartDataSet chartDataSet = new ChartDataSet();
			chartDataSet.setDataSet(sqlDataSet);
			chartDataSet.setPropertySigns(propertySigns);

			chartDataSets.add(chartDataSet);
		}

		entity.setChartDataSets(chartDataSets.toArray(new ChartDataSet[chartDataSets.size()]));
	}

	protected void checkSaveEntity(HtmlChartWidgetEntity chart)
	{
		if (isBlank(chart.getName()))
			throw new IllegalInputException();

		if (isEmpty(chart.getChartPlugin()))
			throw new IllegalInputException();

		if (isEmpty(chart.getChartPlugin()))
			throw new IllegalInputException();
	}
}
