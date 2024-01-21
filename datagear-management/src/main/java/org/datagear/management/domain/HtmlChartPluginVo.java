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

import java.util.List;
import java.util.Map;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.analysis.support.html.HtmlChart;
import org.datagear.analysis.support.html.HtmlChartPlugin;
import org.datagear.analysis.support.html.HtmlChartPluginScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlChartScriptObjectWriter;
import org.datagear.analysis.support.html.HtmlRenderContextScriptObjectWriter;
import org.datagear.analysis.support.html.JsChartRenderer;
import org.datagear.util.i18n.Label;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain HtmlChartPlugin} 值类。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginVo extends HtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	public HtmlChartPluginVo()
	{
		super();
	}

	public HtmlChartPluginVo(String id)
	{
		super.setId(id);
	}

	public HtmlChartPluginVo(String id, Label nameLabel)
	{
		super.setId(id);
		super.setNameLabel(nameLabel);
	}

	@JsonIgnore
	@Override
	public JsChartRenderer getRenderer()
	{
		return super.getRenderer();
	}

	@JsonIgnore
	@Override
	public void setRenderer(JsChartRenderer renderer)
	{
		super.setRenderer(renderer);
	}

	@JsonIgnore
	@Override
	public HtmlChartPluginScriptObjectWriter getPluginWriter()
	{
		return super.getPluginWriter();
	}

	@JsonIgnore
	@Override
	public void setPluginWriter(HtmlChartPluginScriptObjectWriter pluginWriter)
	{
		super.setPluginWriter(pluginWriter);
	}

	@JsonIgnore
	@Override
	public HtmlRenderContextScriptObjectWriter getRenderContextWriter()
	{
		return super.getRenderContextWriter();
	}

	@JsonIgnore
	@Override
	public void setRenderContextWriter(HtmlRenderContextScriptObjectWriter renderContextWriter)
	{
		super.setRenderContextWriter(renderContextWriter);
	}

	@JsonIgnore
	@Override
	public HtmlChartScriptObjectWriter getChartWriter()
	{
		return super.getChartWriter();
	}

	@JsonIgnore
	@Override
	public void setChartWriter(HtmlChartScriptObjectWriter chartWriter)
	{
		super.setChartWriter(chartWriter);
	}

	@JsonIgnore
	@Override
	public List<ChartPluginResource> getResources()
	{
		return super.getResources();
	}

	@JsonIgnore
	@Override
	public void setResources(List<? extends ChartPluginResource> resources)
	{
		super.setResources(resources);
	}

	@JsonIgnore
	@Override
	public Map<String, String> getIconResourceNames()
	{
		return super.getIconResourceNames();
	}

	@JsonIgnore
	@Override
	public void setIconResourceNames(Map<String, String> iconResourceNames)
	{
		super.setIconResourceNames(iconResourceNames);
	}

	@JsonIgnore
	@Override
	public String getElementTagName()
	{
		return super.getElementTagName();
	}

	@JsonIgnore
	@Override
	public void setElementTagName(String elementTagName)
	{
		super.setElementTagName(elementTagName);
	}

	@JsonIgnore
	@Override
	public long getLastModified()
	{
		return super.getLastModified();
	}

	@JsonIgnore
	@Override
	public void setLastModified(long lastModified)
	{
		super.setLastModified(lastModified);
	}

	@JsonIgnore
	@Override
	public String getNewLine()
	{
		return super.getNewLine();
	}

	@JsonIgnore
	@Override
	public void setNewLine(String newLine)
	{
		super.setNewLine(newLine);
	}

	@Override
	public HtmlChart renderChart(ChartDefinition chartDefinition, RenderContext renderContext) throws RenderException
	{
		throw new UnsupportedOperationException();
	}
}
