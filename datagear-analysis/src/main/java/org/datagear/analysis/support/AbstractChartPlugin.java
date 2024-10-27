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
import org.datagear.analysis.Category;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartPluginAttribute;
import org.datagear.analysis.ChartPluginDataSetRange;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.DataSign;
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
	
	private Map<String, String> iconResourceNames = Collections.emptyMap();

	private List<ChartPluginAttribute> attributes = Collections.emptyList();

	private List<DataSign> dataSigns = Collections.emptyList();

	private ChartPluginDataSetRange dataSetRange = null;

	private String version = "";

	private int order = 0;

	private List<Category> categories = Collections.emptyList();

	private List<Integer> categoryOrders = Collections.emptyList();

	private String author = "";

	private String contact = "";

	private String issueDate = "";

	private String platformVersion = "";

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
		return NameAwareUtil.find(this.resources, name);
	}
	
	@Override
	public String getIconResourceName(String themeName)
	{
		if (this.iconResourceNames == null || this.iconResourceNames.isEmpty())
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

		return resName;
	}

	@Override
	public List<ChartPluginAttribute> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(List<ChartPluginAttribute> attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public ChartPluginAttribute getAttribute(String name)
	{
		return NameAwareUtil.find(this.attributes, name);
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
	public String getPlatformVersion()
	{
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion)
	{
		this.platformVersion = platformVersion;
	}
}
