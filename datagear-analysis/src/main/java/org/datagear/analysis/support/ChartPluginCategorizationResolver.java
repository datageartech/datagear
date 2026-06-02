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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datagear.analysis.Category;
import org.datagear.analysis.CategoryJoin;
import org.datagear.analysis.ChartPlugin;
import org.datagear.util.StringUtil;

/**
 * {@linkplain ChartPlugin}分类处理器。
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginCategorizationResolver
{
	public ChartPluginCategorizationResolver()
	{
		super();
	}

	/**
	 * 分类。
	 * 
	 * @param chartPlugins
	 * @return 最后一个元素包含所有未分组的{@linkplain ChartPlugin}
	 */
	public List<Categorization> resolve(List<? extends ChartPlugin> chartPlugins)
	{
		List<CategorizationInfo> categorizationInfos = new ArrayList<>();
		List<ChartPluginInfo> uncategorizeds = new ArrayList<>();

		for (ChartPlugin chartPlugin : chartPlugins)
		{
			List<CategoryJoin> categoryJoins = chartPlugin.getCategoryJoins();

			boolean added = false;

			if (categoryJoins != null)
			{
				for (int i = 0; i < categoryJoins.size(); i++)
				{
					CategoryJoin categoryJoin = categoryJoins.get(i);
					Category category = categoryJoin.getCategory();
					int order = categoryJoin.getOrder();

					if (category == null)
						continue;

					CategorizationInfo categorizationInfo = null;

					for (CategorizationInfo myCategorizationInfo : categorizationInfos)
					{
						Category myCategory = myCategorizationInfo.getCategory();

						if (StringUtil.isEquals(category.getName(), myCategory.getName()))
						{
							categorizationInfo = myCategorizationInfo;

							// 使用信息最全的那个
							if (category.hasNameLabel())
								categorizationInfo.setCategory(category);
						}
					}

					if (categorizationInfo == null)
					{
						categorizationInfo = new CategorizationInfo(category);
						categorizationInfos.add(categorizationInfo);
					}

					categorizationInfo
							.addChartPluginInfo(new ChartPluginInfo(chartPlugin, order, chartPlugin.getOrder()));
					added = true;
				}
			}

			if (!added)
				uncategorizeds.add(new ChartPluginInfo(chartPlugin, 0, chartPlugin.getOrder()));
		}

		Collections.sort(categorizationInfos);

		if (!uncategorizeds.isEmpty())
		{
			CategorizationInfo uncategorized = new CategorizationInfo(new Category(""));
			uncategorized.setChartPluginInfos(uncategorizeds);
			categorizationInfos.add(uncategorized);
		}

		return toCategorizations(categorizationInfos);
	}

	protected List<Categorization> toCategorizations(List<CategorizationInfo> categorizationInfos)
	{
		List<Categorization> categorizations = new ArrayList<Categorization>(categorizationInfos.size());

		for (CategorizationInfo ci : categorizationInfos)
			categorizations.add(ci.toCategorization());

		return categorizations;
	}

	protected static class CategorizationInfo implements Comparable<CategorizationInfo>
	{
		private Category category;

		private List<ChartPluginInfo> chartPluginInfos = new ArrayList<>(5);

		public CategorizationInfo()
		{
			super();
		}

		public CategorizationInfo(Category category)
		{
			super();
			this.category = category;
		}

		public Category getCategory()
		{
			return category;
		}

		public void setCategory(Category category)
		{
			this.category = category;
		}

		public List<ChartPluginInfo> getChartPluginInfos()
		{
			return chartPluginInfos;
		}

		public void setChartPluginInfos(List<ChartPluginInfo> chartPluginInfos)
		{
			this.chartPluginInfos = chartPluginInfos;
		}

		public void addChartPluginInfo(ChartPluginInfo cpi)
		{
			this.chartPluginInfos.add(cpi);
		}

		@Override
		public int compareTo(CategorizationInfo o)
		{
			return getCategory().getOrder() - o.getCategory().getOrder();
		}

		public Categorization toCategorization()
		{
			Categorization categorization = new Categorization(this.category);

			List<ChartPluginInfo> chartPluginInfos = new ArrayList<ChartPluginInfo>(this.chartPluginInfos);
			Collections.sort(chartPluginInfos);

			List<ChartPlugin> chartPlugins = new ArrayList<ChartPlugin>(chartPluginInfos.size());

			for (ChartPluginInfo cpi : chartPluginInfos)
				chartPlugins.add(cpi.getChartPlugin());

			categorization.setChartPlugins(chartPlugins);

			return categorization;
		}
	}

	protected static class ChartPluginInfo implements Comparable<ChartPluginInfo>
	{
		private final ChartPlugin chartPlugin;

		private final int categoryOrder;

		private final int order;

		public ChartPluginInfo(ChartPlugin chartPlugin, int categoryOrder, int order)
		{
			super();
			this.chartPlugin = chartPlugin;
			this.categoryOrder = categoryOrder;
			this.order = order;
		}

		public ChartPlugin getChartPlugin()
		{
			return chartPlugin;
		}

		public int getCategoryOrder()
		{
			return categoryOrder;
		}

		public int getOrder()
		{
			return order;
		}

		@Override
		public int compareTo(ChartPluginInfo o)
		{
			int re = this.categoryOrder - o.categoryOrder;

			if (re == 0)
				re = this.order - o.order;

			return re;
		}
	}

	/**
	 * 分类结果。
	 */
	public static class Categorization
	{
		private Category category;

		private List<ChartPlugin> chartPlugins = new ArrayList<>(5);

		public Categorization()
		{
			super();
		}

		public Categorization(Category category)
		{
			super();
			this.category = category;
		}

		public Category getCategory()
		{
			return category;
		}

		public void setCategory(Category category)
		{
			this.category = category;
		}

		public List<ChartPlugin> getChartPlugins()
		{
			return chartPlugins;
		}

		public void setChartPlugins(List<ChartPlugin> chartPlugins)
		{
			this.chartPlugins = chartPlugins;
		}
	}
}
