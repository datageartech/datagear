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

package org.datagear.analysis.support.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.ChartPluginResource;
import org.datagear.analysis.RenderContext;
import org.datagear.analysis.RenderException;
import org.datagear.util.i18n.Label;
import org.datagear.util.i18n.LabelUtil;
import org.datagear.util.i18n.Localizable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 用于输出JSON的{@linkplain HtmlChartPlugin}。
 * 
 * @author datagear@163.com
 *
 */
public class HtmlChartPluginJson extends HtmlChartPlugin
{
	private static final long serialVersionUID = 1L;

	public HtmlChartPluginJson()
	{
		super();
	}

	public HtmlChartPluginJson(String id, Label nameLabel)
	{
		super(id, nameLabel, null, null, null, null);
	}

	public HtmlChartPluginJson(HtmlChartPlugin plugin)
	{
		super(plugin);
		setResources(ChartPluginResourceJson.valuesOf(plugin.getResources()));
	}

	public HtmlChartPluginJson(HtmlChartPlugin plugin, Locale locale)
	{
		this(plugin);
		initLocalized(plugin, locale);
	}

	protected void initLocalized(HtmlChartPlugin plugin, Locale locale)
	{
		LabelUtil.concrete(plugin, this, locale);
		setDataSigns(Localizable.toLocale(plugin.getDataSigns(), locale));
		setAttributeForm(plugin.getAttributeForm() == null ? null : plugin.getAttributeForm().toLocale(locale));
		setCategories(Localizable.toLocale(plugin.getCategories(), locale));
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

	@JsonIgnore
	@Override
	public HtmlChart renderChart(ChartDefinition chartDefinition, RenderContext renderContext)
			throws RenderException
	{
		throw new UnsupportedOperationException();
	}

	protected static class ChartPluginResourceJson implements ChartPluginResource, Serializable
	{
		private static final long serialVersionUID = 1L;

		private String name;

		public ChartPluginResourceJson()
		{
			super();
		}

		public ChartPluginResourceJson(String name)
		{
			super();
			this.name = name;
		}

		public ChartPluginResourceJson(ChartPluginResource resource)
		{
			super();
			this.name = (resource == null ? null : resource.getName());
		}

		@Override
		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@JsonIgnore
		@Override
		public InputStream getInputStream() throws IOException
		{
			return null;
		}

		@JsonIgnore
		@Override
		public long getLastModified()
		{
			return 0;
		}

		public static List<ChartPluginResourceJson> valuesOf(List<? extends ChartPluginResource> resources)
		{
			if (resources == null)
				return null;
			else if (resources.isEmpty())
				return Collections.emptyList();

			List<ChartPluginResourceJson> resJsons = new ArrayList<ChartPluginResourceJson>(resources.size());

			for (ChartPluginResource resource : resources)
				resJsons.add(new ChartPluginResourceJson(resource));

			return resJsons;
		}
	}
}