/*
 * Copyright 2018-2023 datagear.tech
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

package org.datagear.management.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.JsonSupport;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartWidget;
import org.datagear.util.StringUtil;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * HTML {@linkplain ChartWidget}实体。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartWidgetEntity extends HtmlChartWidget
		implements CreateUserEntity<String>, DataPermissionEntity<String>, AnalysisProjectAwareEntity<String>,
		CloneableEntity
{
	private static final long serialVersionUID = 1L;

	/** 授权资源类型 */
	public static final String AUTHORIZATION_RESOURCE_TYPE = "Chart";

	protected static final ChartDataSetVO[] EMPTY_CHART_DATA_VO_SET = new ChartDataSetVO[0];

	/** 图表部件渲染时的图表选项信息 */
	public static final String ATTR_CHART_OPTIONS = BUILTIN_ATTR_PREFIX + "CHART_OPTIONS";

	private HtmlChartPluginVo pluginVo;

	/**
	 * 图表选项。
	 * <p>
	 * 这里不应定义为Map类型，因为options里可能包含JS函数定义，无法转换为JSON字符串，转换工作由界面处理。
	 * </p>
	 */
	private String options = "";

	/** 创建用户 */
	private User createUser;

	/** 创建时间 */
	private Date createTime;

	/** 权限 */
	private int dataPermission = PERMISSION_NOT_LOADED;

	private AnalysisProject analysisProject = null;

	public HtmlChartWidgetEntity()
	{
		super();
		super.setChartDataSets(EMPTY_CHART_DATA_VO_SET);
		this.createTime = new Date();
	}

	public HtmlChartWidgetEntity(String id, String name, ChartDataSetVO[] chartDataSets, HtmlChartPlugin chartPlugin,
			User createUser)
	{
		super(id, name, chartDataSets, chartPlugin);
		super.setChartDataSets(EMPTY_CHART_DATA_VO_SET);
		this.createUser = createUser;
		this.createTime = new Date();
	}

	public String getOptions()
	{
		return options;
	}

	public void setOptions(String options)
	{
		this.options = options;
	}

	public ChartDataSetVO[] getChartDataSetVOs()
	{
		return (ChartDataSetVO[]) super.getChartDataSets();
	}

	public void setChartDataSetVOs(ChartDataSetVO[] chartDataSetVOs)
	{
		super.setChartDataSets(chartDataSetVOs);
	}

	public HtmlChartPluginVo getPluginVo()
	{
		return this.pluginVo;
	}

	public void setPluginVo(HtmlChartPluginVo pluginVo)
	{
		this.pluginVo = pluginVo;
	}

	@JsonIgnore
	@Override
	public HtmlChartPlugin getPlugin()
	{
		return super.getPlugin();
	}

	@JsonIgnore
	@Override
	public void setPlugin(ChartPlugin plugin)
	{
		super.setPlugin(plugin);
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

	@Override
	public AnalysisProject getAnalysisProject()
	{
		return analysisProject;
	}

	@Override
	public void setAnalysisProject(AnalysisProject analysisProject)
	{
		this.analysisProject = analysisProject;
	}

	/**
	 * 获取{@linkplain #getAttrValues()}的JSON字符串形式。
	 * <p>
	 * 目前仅用于Mybatis持久存储。
	 * </p>
	 * 
	 * @return
	 */
	@JsonIgnore
	public String getAttrValuesJson()
	{
		Map<String, ?> attrValues = getAttrValues();
		
		if(attrValues == null)
			return null;
		else
			return JsonSupport.generate(attrValues, "");
	}

	/**
	 * 设置{@linkplain #setAttrValues(Map)}的JSON字符串形式。
	 * <p>
	 * 目前仅用于Mybatis持久存储。
	 * </p>
	 * 
	 * @param attrValuesJson
	 */
	@JsonIgnore
	@SuppressWarnings("unchecked")
	public void setAttrValuesJson(String attrValuesJson)
	{
		Map<String, Object> attrValues = null;
		
		if(!StringUtil.isEmpty(attrValuesJson))
			attrValues = JsonSupport.parse(attrValuesJson, Map.class, null);
		
		setAttrValues(attrValues);
	}

	@Override
	protected ChartDefinition buildChartDefinition(String id) throws RenderException
	{
		ChartDefinition chartDefinition = super.buildChartDefinition(id);
		chartDefinition.setAttrValue(ATTR_CHART_OPTIONS, this.options);
		
		return chartDefinition;
	}

	@Override
	public HtmlChartWidgetEntity clone()
	{
		HtmlChartWidgetEntity entity = new HtmlChartWidgetEntity();
		BeanUtils.copyProperties(this, entity);

		ChartDataSetVO[] chartDataSetVOs = entity.getChartDataSetVOs();

		if (chartDataSetVOs != null && chartDataSetVOs.length != 0)
		{
			ChartDataSetVO[] cloned = new ChartDataSetVO[chartDataSetVOs.length];

			for (int i = 0; i < chartDataSetVOs.length; i++)
			{
				cloned[i] = chartDataSetVOs[i].clone();
			}

			entity.setChartDataSetVOs(cloned);
		}

		Map<String, Object> attrValues = this.getAttrValues();
		if (attrValues != null)
		{
			Map<String, Object> attrValuesClone = new HashMap<String, Object>(attrValues);
			entity.setAttrValues(attrValuesClone);
		}

		return entity;
	}
}
