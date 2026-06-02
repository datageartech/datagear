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

package org.datagear.analysis.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.CategoryJoin;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginConfigForm;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DataSignSpec;
import org.datagear.analysis.NameAwareUtil;
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
	private static final long serialVersionUID = 1L;

	private Label nameLabel;

	private Label descLabel = null;

	private List<ChartPluginResource> resources = Collections.emptyList();
	
	/** 图标资源名映射表，名为主题名，值为图标URL */
	private Map<String, String> icons = Collections.emptyMap();

	private ChartPluginConfigForm configForm = null;

	private DataSignSpec dataSignSpec = null;

	private ChartPluginDataSetRange dataSetRange = null;

	private String version = "";

	private int order = 0;

	private List<CategoryJoin> categoryJoins = null;

	private String author = "";

	private String contact = "";

	private String issueDate = "";

	private Map<String, ?> additions = null;

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

	@SuppressWarnings("unchecked")
	public void setResources(List<? extends ChartPluginResource> resources)
	{
		this.resources = (List<ChartPluginResource>) resources;
	}

	public Map<String, String> getIcons()
	{
		return icons;
	}

	public void setIcons(Map<String, String> icons)
	{
		this.icons = icons;
	}

	@Override
	public ChartPluginResource getResource(String name)
	{
		return NameAwareUtil.find(this.resources, name);
	}
	
	@Override
	public String getIconResourceName(String themeName)
	{
		if (this.icons == null || this.icons.isEmpty())
			return null;

		themeName = (themeName == null ? "" : themeName.toLowerCase());

		String firstResName = null;
		String exactResName = null;
		String likeResName = null;

		Map<String, String> lowerKeyMap = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : this.icons.entrySet())
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
			resName = this.icons.get(DEFAULT_ICON_THEME_NAME);

		if (StringUtil.isEmpty(resName))
			resName = firstResName;

		return resName;
	}

	@Override
	public ChartPluginConfigForm getConfigForm()
	{
		return configForm;
	}

	public void setConfigForm(ChartPluginConfigForm configForm)
	{
		this.configForm = configForm;
	}

	@Override
	public DataSignSpec getDataSignSpec()
	{
		return dataSignSpec;
	}

	public void setDataSignSpec(DataSignSpec dataSignSpec)
	{
		this.dataSignSpec = dataSignSpec;
	}

	@Override
	public ChartPluginDataSetRange getDataSetRange()
	{
		return dataSetRange;
	}

	public void setDataSetRange(ChartPluginDataSetRange dataSetRange)
	{
		this.dataSetRange = dataSetRange;
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
	public List<CategoryJoin> getCategoryJoins()
	{
		return categoryJoins;
	}

	public void setCategoryJoins(List<CategoryJoin> categoryJoins)
	{
		this.categoryJoins = categoryJoins;
	}

	@Override
	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	@Override
	public String getContact()
	{
		return contact;
	}

	public void setContact(String contact)
	{
		this.contact = contact;
	}

	@Override
	public String getIssueDate()
	{
		return issueDate;
	}

	public void setIssueDate(String issueDate)
	{
		this.issueDate = issueDate;
	}

	@Override
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}
}
