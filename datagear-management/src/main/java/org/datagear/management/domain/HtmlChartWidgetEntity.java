/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.domain;

import java.util.Date;
import java.util.Locale;

import org.datagear.analysis.ChartDataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.analysis.support.html.HtmlRenderContext;
import org.datagear.util.i18n.Label;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * HTML {@linkplain ChartWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetEntity extends HtmlChartWidget<HtmlRenderContext>
		implements CreateUserEntity<String>, DataPermissionEntity<String>
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "Chart";

	protected static final ChartDataSetVO[] EMPTY_CHART_DATA_VO_SET = new ChartDataSetVO[0];

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	/** 插件名称，展示用 */
	private String chartPluginName = "";

	public HtmlChartWidgetEntity()
	{
		super();
		super.setChartDataSets(EMPTY_CHART_DATA_VO_SET);
		this.createTime = new Date();
	}

	public HtmlChartWidgetEntity(String id, String name, ChartDataSetVO[] chartDataSets,
			HtmlChartPlugin<HtmlRenderContext> chartPlugin, User createUser)
	{
		super(id, name, chartDataSets, chartPlugin);
		super.setChartDataSets(EMPTY_CHART_DATA_VO_SET);
		this.createUser = createUser;
		this.createTime = new Date();
	}

	@Override
	public ChartDataSetVO[] getChartDataSets()
	{
		return (ChartDataSetVO[]) super.getChartDataSets();
	}

	@Override
	public void setChartDataSets(ChartDataSet[] chartDataSets)
	{
		if (chartDataSets != null && !(chartDataSets instanceof ChartDataSetVO[]))
			throw new IllegalArgumentException();

		super.setChartDataSets(chartDataSets);
	}

	public HtmlChartPlugin<HtmlRenderContext> getHtmlChartPlugin()
	{
		return getPlugin();
	}

	public void setHtmlChartPlugin(HtmlChartPlugin<HtmlRenderContext> htmlChartPlugin)
	{
		setPlugin(htmlChartPlugin);
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public void setCreateUser(User createUser)
	{
		this.createUser = createUser;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	@Override
	public int getDataPermission()
	{
		return dataPermission;
	}

	@Override
	public void setDataPermission(int dataPermission)
	{
		this.dataPermission = dataPermission;
	}

	public String getChartPluginName()
	{
		return chartPluginName;
	}

	public void setChartPluginName(String chartPluginName)
	{
		this.chartPluginName = chartPluginName;
	}

	@JsonIgnore
	@Override
	public DataSetResult[] getDataSetResults() throws DataSetException
	{
		return super.getDataSetResults();
	}

	public void updateChartPluginName(Locale locale)
	{
		String name = null;
		HtmlChartPlugin<HtmlRenderContext> plugin = getHtmlChartPlugin();

		if (plugin != null)
		{
			Label nameLabel = plugin.getNameLabel();
			if (nameLabel != null)
				name = nameLabel.getValue(locale);
		}

		setChartPluginName(name);
	}
}
