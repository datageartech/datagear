/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.datagear.analysis.Category;
import org.datagear.analysis.ChartPlugin;
import org.datagear.util.StringUtil;

/**
 * 将{@linkplain ChartPlugin}按照{@linkplain Category}分组处理器。
 * <p>
 * 此类是线程安全的。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class CategorizationResolver
{
	public CategorizationResolver()
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
		List<Categorization> categorizations = new ArrayList<>();
		List<ChartPlugin> uncategorizeds = new ArrayList<>();

		for (ChartPlugin chartPlugin : chartPlugins)
		{
			Category category = chartPlugin.getCategory();

			if (category == null || StringUtil.isEmpty(category.getName()))
				uncategorizeds.add(chartPlugin);
			else
			{
				Categorization categorization = null;

				for (Categorization myCategorization : categorizations)
				{
					Category myCategory = myCategorization.getCategory();

					if (category.getName().equals(myCategory.getName()))
					{
						categorization = myCategorization;

						// 使用信息最全的那个
						if (category.hasNameLabel())
							categorization.setCategory(category);
					}
				}

				if (categorization == null)
				{
					categorization = new Categorization(category);
					categorizations.add(categorization);
				}

				categorization.addChartPlugin(chartPlugin);
			}
		}

		Collections.sort(categorizations, CATEGORIZATION_COMPARATOR);

		if (!uncategorizeds.isEmpty())
		{
			Categorization uncategorized = new Categorization(new Category(""));
			uncategorized.setChartPlugins(uncategorizeds);
			categorizations.add(uncategorized);
		}

		return categorizations;
	}

	protected static final Comparator<Categorization> CATEGORIZATION_COMPARATOR = new Comparator<Categorization>()
	{
		@Override
		public int compare(Categorization o1, Categorization o2)
		{
			return o1.getCategory().getOrder() - o2.getCategory().getOrder();
		}
	};

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

		public void addChartPlugin(ChartPlugin chartPlugin)
		{
			this.chartPlugins.add(chartPlugin);
		}
	}
}
