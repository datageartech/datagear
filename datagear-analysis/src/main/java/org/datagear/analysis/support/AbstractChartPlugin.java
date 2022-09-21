/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.Category;
import org.datagear.analysis.ChartParam;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DataSign;
import org.datagear.util.StringUtil;
import org.datagear.util.i18n.Label;

/**
 * 抽象{@linkplain ChartPlugin}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartPlugin extends AbstractIdentifiable implements ChartPlugin
{
	private Label nameLabel;

	private Label descLabel = null;

	private List<ChartPluginResource> resources = Collections.emptyList();
	
	private Map<String, String> iconResourceNames = Collections.emptyMap();

	private List<ChartParam> chartParams = Collections.emptyList();

	private List<DataSign> dataSigns = Collections.emptyList();

	private String version = "";

	private int order = 0;

	private List<Category> categories = Collections.emptyList();;

	private List<Integer> categoryOrders = Collections.emptyList();;

	public AbstractChartPlugin()
	{
	}

	public AbstractChartPlugin(String id, Label nameLabel)
	{
		super(id);
		this.nameLabel = nameLabel;
	}

	@Override
	public Label getNameLabel()
	{
		return nameLabel;
	}

	@Override
	public void setNameLabel(Label nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	@Override
	public Label getDescLabel()
	{
		return descLabel;
	}

	@Override
	public void setDescLabel(Label descLabel)
	{
		this.descLabel = descLabel;
	}

	@Override
	public List<ChartPluginResource> getResources()
	{
		return resources;
	}

	public void setResources(List<ChartPluginResource> resources)
	{
		this.resources = resources;
	}

	public Map<String, String> getIconResourceNames()
	{
		return iconResourceNames;
	}

	public void setIconResourceNames(Map<String, String> iconResourceNames)
	{
		this.iconResourceNames = iconResourceNames;
	}

	@Override
	public ChartPluginResource getResource(String name)
	{
		if (this.resources == null)
			return null;

		for (ChartPluginResource res : this.resources)
		{
			if (res.getName().equals(name))
				return res;
		}

		return null;
	}

	@Override
	public ChartPluginResource getIconResource(String themeName)
	{
		if (this.iconResourceNames == null)
			return null;

		themeName = (themeName == null ? "" : themeName.toLowerCase());

		String firstResName = null;
		String exactResName = null;
		String likeResName = null;

		Map<String, String> lowerKeyMap = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : this.iconResourceNames.entrySet())
			lowerKeyMap.put(entry.getKey().toLowerCase(), entry.getValue());

		for (Map.Entry<String, String> entry : lowerKeyMap.entrySet())
		{
			String myThemeName = entry.getKey();
			String myResName = entry.getValue();

			if (StringUtil.isEmpty(firstResName))
				firstResName = myResName;

			if (myThemeName.equals(themeName))
				exactResName = myResName;
			else if (myThemeName.indexOf(themeName) > -1 || themeName.indexOf(myThemeName) > -1)
				likeResName = myResName;
		}

		String resName = null;

		if (!StringUtil.isEmpty(exactResName))
			resName = exactResName;
		else if (!StringUtil.isEmpty(likeResName))
			resName = likeResName;
		else
			resName = this.iconResourceNames.get(DEFAULT_ICON_THEME_NAME);

		if (StringUtil.isEmpty(resName))
			resName = firstResName;

		return (StringUtil.isEmpty(resName) ? null : getResource(resName));
	}

	@Override
	public List<ChartParam> getChartParams()
	{
		return chartParams;
	}

	public void setChartParams(List<ChartParam> chartParams)
	{
		this.chartParams = chartParams;
	}

	@Override
	public ChartParam getChartParam(String name)
	{
		if (this.chartParams == null)
			return null;

		for (ChartParam chartParam : this.chartParams)
		{
			if (chartParam.getName().equals(name))
				return chartParam;
		}

		return null;
	}

	@Override
	public List<DataSign> getDataSigns()
	{
		return dataSigns;
	}

	public void setDataSigns(List<DataSign> dataSigns)
	{
		this.dataSigns = dataSigns;
	}

	@Override
	public DataSign getDataSign(String name)
	{
		if (this.dataSigns == null)
			return null;

		for (DataSign dataSign : this.dataSigns)
		{
			if (dataSign.getName().equals(name))
				return dataSign;
		}

		return null;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	@Override
	public List<Category> getCategories()
	{
		return categories;
	}

	public void setCategories(List<Category> categories)
	{
		this.categories = categories;
	}

	@Override
	public List<Integer> getCategoryOrders()
	{
		return categoryOrders;
	}

	public void setCategoryOrders(List<Integer> categoryOrders)
	{
		this.categoryOrders = categoryOrders;
	}
}
