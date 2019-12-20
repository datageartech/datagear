/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.web.controller;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginManager;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.management.domain.HtmlChartWidgetEntity;
import org.datagear.management.domain.SqlDataSetFactoryEntity;
import org.datagear.management.domain.User;
import org.datagear.management.service.HtmlChartWidgetEntityService;
import org.datagear.persistence.PagingData;
import org.datagear.persistence.PagingQuery;
import org.datagear.util.IDUtil;
import org.datagear.util.IOUtil;
import org.datagear.util.i18n.Label;
import org.datagear.web.OperationMessage;
import org.datagear.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 数据集控制器。
 * 
 * @author datagear@163.com
 *
 */
@Controller
@RequestMapping("/analysis/chart")
public class ChartController extends AbstractController
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

		List<HtmlChartPluginInfo> pluginInfos = getAllHtmlChartPluginInfos(request);

		if (pluginInfos.size() > 0)
			chart.setChartPlugin(pluginInfos.get(0).getChartPlugin());

		model.addAttribute("chart", chart);
		model.addAttribute("pluginInfos", pluginInfos);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.addChart");
		model.addAttribute(KEY_FORM_ACTION, "saveAdd");

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/saveAdd", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveAdd(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity chart)
	{
		checkSaveEntity(chart);

		chart.setId(IDUtil.uuid());
		chart.setCreateUser(WebUtils.getUser(request, response));
		chart.setSqlDataSetFactoryEntities(getSqlDataSetFactoryEntityParams(request));

		this.htmlChartWidgetEntityService.add(chart);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/edit")
	public String edit(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(id);

		List<HtmlChartPluginInfo> pluginInfos = getAllHtmlChartPluginInfos(request);

		model.addAttribute("chart", chart);
		model.addAttribute("pluginInfos", pluginInfos);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.editChart");
		model.addAttribute(KEY_FORM_ACTION, "saveEdit");

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/saveEdit", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> saveEdit(HttpServletRequest request, HttpServletResponse response,
			HtmlChartWidgetEntity chart)
	{
		checkSaveEntity(chart);

		chart.setSqlDataSetFactoryEntities(getSqlDataSetFactoryEntityParams(request));

		this.htmlChartWidgetEntityService.update(chart);

		return buildOperationMessageSaveSuccessResponseEntity(request);
	}

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response, org.springframework.ui.Model model,
			@RequestParam("id") String id)
	{
		HtmlChartWidgetEntity chart = this.htmlChartWidgetEntityService.getById(id);

		if (chart == null)
			throw new RecordNotFoundException();

		List<HtmlChartPluginInfo> pluginInfos = getAllHtmlChartPluginInfos(request);

		model.addAttribute("chart", chart);
		model.addAttribute("pluginInfos", pluginInfos);
		model.addAttribute(KEY_TITLE_MESSAGE_KEY, "chart.viewChart");
		model.addAttribute(KEY_READONLY, true);

		return "/analysis/chart/chart_form";
	}

	@RequestMapping(value = "/delete", produces = CONTENT_TYPE_JSON)
	@ResponseBody
	public ResponseEntity<OperationMessage> delete(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("id") String[] ids)
	{
		this.htmlChartWidgetEntityService.deleteByIds(ids);

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

		return pagingData;
	}

	@RequestMapping(value = "/pluginicon/{pluginId}", produces = CONTENT_TYPE_JSON)
	public void getPluginIcon(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("pluginId") String pluginId) throws Exception
	{
		ChartPlugin<?> chartPlugin = this.chartPluginManager.get(pluginId);

		if (chartPlugin == null)
			throw new FileNotFoundException();

		RenderStyle renderStyle = getRenderStyle(request);
		Icon icon = chartPlugin.getIcon(renderStyle);

		if (icon == null)
			throw new FileNotFoundException();

		response.setContentType("image/png");

		OutputStream out = response.getOutputStream();
		InputStream iconIn = null;

		try
		{
			iconIn = icon.getInputStream();
			IOUtil.write(iconIn, out);
		}
		finally
		{
			IOUtil.close(iconIn);
		}
	}

	protected SqlDataSetFactoryEntity[] getSqlDataSetFactoryEntityParams(HttpServletRequest request)
	{
		String[] dataSetIds = request.getParameterValues("dataSetId");

		if (dataSetIds == null || dataSetIds.length == 0)
			return null;

		SqlDataSetFactoryEntity[] params = new SqlDataSetFactoryEntity[dataSetIds.length];

		for (int i = 0; i < dataSetIds.length; i++)
		{
			SqlDataSetFactoryEntity param = new SqlDataSetFactoryEntity();
			param.setId(dataSetIds[i]);

			params[i] = param;
		}

		return params;
	}

	protected void checkSaveEntity(HtmlChartWidgetEntity chart)
	{
		if (isBlank(chart.getName()))
			throw new IllegalInputException();

		if (isEmpty(chart.getChartPlugin()))
			throw new IllegalInputException();
	}

	protected List<HtmlChartPluginInfo> getAllHtmlChartPluginInfos(HttpServletRequest request)
	{
		List<HtmlChartPluginInfo> pluginInfos = new ArrayList<HtmlChartPluginInfo>();

		List<ChartPlugin<HtmlRenderContext>> plugins = this.chartPluginManager.getAll(HtmlRenderContext.class);

		if (plugins != null)
		{
			Locale locale = WebUtils.getLocale(request);
			RenderStyle renderStyle = getRenderStyle(request);

			for (ChartPlugin<HtmlRenderContext> plugin : plugins)
			{
				if (plugin instanceof HtmlChartPlugin<?>)
				{
					HtmlChartPluginInfo pluginInfo = new HtmlChartPluginInfo();
					pluginInfo.setChartPlugin((HtmlChartPlugin<HtmlRenderContext>) plugin);

					Label nameLabel = plugin.getNameLabel();
					if (nameLabel != null)
						pluginInfo.setName(nameLabel.getValue(locale));

					Label descLabel = plugin.getDescLabel();
					if (descLabel != null)
						pluginInfo.setDesc(descLabel.getValue(locale));

					Label manualLabel = plugin.getManualLabel();
					if (manualLabel != null)
						pluginInfo.setManual(manualLabel.getValue(locale));

					Icon icon = plugin.getIcon(renderStyle);
					pluginInfo.setHasIcon(icon != null);

					pluginInfos.add(pluginInfo);
				}
			}
		}

		return pluginInfos;
	}

	protected RenderStyle getRenderStyle(HttpServletRequest request)
	{
		return RenderStyle.LIGHT;
	}

	public static class HtmlChartPluginInfo implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private HtmlChartPlugin<HtmlRenderContext> chartPlugin;
		private String name;
		private String desc;
		private String manual;
		private boolean hasIcon;

		public HtmlChartPluginInfo()
		{
			super();
		}

		public HtmlChartPluginInfo(HtmlChartPlugin<HtmlRenderContext> chartPlugin, String name)
		{
			super();
			this.chartPlugin = chartPlugin;
			this.name = name;
		}

		public HtmlChartPlugin<HtmlRenderContext> getChartPlugin()
		{
			return chartPlugin;
		}

		public void setChartPlugin(HtmlChartPlugin<HtmlRenderContext> chartPlugin)
		{
			this.chartPlugin = chartPlugin;
		}

		public String getId()
		{
			return this.chartPlugin.getId();
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDesc()
		{
			return desc;
		}

		public void setDesc(String desc)
		{
			this.desc = desc;
		}

		public String getManual()
		{
			return manual;
		}

		public void setManual(String manual)
		{
			this.manual = manual;
		}

		public boolean isHasIcon()
		{
			return hasIcon;
		}

		public void setHasIcon(boolean hasIcon)
		{
			this.hasIcon = hasIcon;
		}
	}
}
