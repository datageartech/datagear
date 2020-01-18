/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.AbstractIdentifiable;
import org.datagear.analysis.ChartPlugin;
import org.datagear.analysis.ChartProperty;
import org.datagear.analysis.DataSign;
import org.datagear.analysis.Icon;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderStyle;
import org.datagear.util.i18n.Label;

/**
 * 抽象{@linkplain ChartPlugin}。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractChartPlugin<T extends RenderContext> extends AbstractIdentifiable
		implements ChartPlugin<T>
{
	private Label nameLabel;

	private Label descLabel;

	private Label manualLabel;

	private Map<RenderStyle, Icon> icons;

	private List<ChartProperty> chartProperties;

	private List<DataSign> dataSigns;

	private String version;

	private int order = 0;

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

	public void setNameLabel(Label nameLabel)
	{
		this.nameLabel = nameLabel;
	}

	@Override
	public Label getDescLabel()
	{
		return descLabel;
	}

	public void setDescLabel(Label descLabel)
	{
		this.descLabel = descLabel;
	}

	@Override
	public Label getManualLabel()
	{
		return manualLabel;
	}

	public void setManualLabel(Label manualLabel)
	{
		this.manualLabel = manualLabel;
	}

	public Map<RenderStyle, Icon> getIcons()
	{
		return icons;
	}

	public void setIcons(Map<RenderStyle, Icon> icons)
	{
		this.icons = icons;
	}

	@Override
	public Icon getIcon(RenderStyle renderStyle)
	{
		Icon icon = (this.icons == null ? null : this.icons.get(renderStyle));

		if (icon == null && !RenderStyle.LIGHT.equals(renderStyle))
			icon = getIcon(RenderStyle.LIGHT);

		return icon;
	}

	@Override
	public List<ChartProperty> getChartProperties()
	{
		return chartProperties;
	}

	public void setChartProperties(List<ChartProperty> chartProperties)
	{
		this.chartProperties = chartProperties;
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

}
