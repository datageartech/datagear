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

package org.datagear.analysis;

import java.io.Serializable;

/**
 * 图表插件数据集范围。
 * <p>
 * {@linkplain ChartPlugin}使用此类声明它的{@linkplain ChartPlugin#renderChart(ChartDefinition, RenderContext)}的{@linkplain ChartDefinition#getDataSetBinds()}的数目范围约束。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPluginDataSetRange implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_MAIN = "main";
	public static final String PROPERTY_ATTACHMENT = "attachment";

	private Range main = null;

	private Range attachment = null;

	public ChartPluginDataSetRange()
	{
		super();
	}

	public ChartPluginDataSetRange(Range main, Range attachment)
	{
		super();
		this.main = main;
		this.attachment = attachment;
	}

	/**
	 * 获取主件数据集数目范围（{@linkplain DataSetBind#isAttachment()}为{@code false}的）。
	 * 
	 * @return {@code null}表示没有限制
	 */
	public Range getMain()
	{
		return main;
	}

	public void setMain(Range main)
	{
		this.main = main;
	}

	/**
	 * 获取附件数据集数目范围（{@linkplain DataSetBind#isAttachment()}为{@code true}的）。
	 * 
	 * @return {@code null}表示没有限制
	 */
	public Range getAttachment()
	{
		return attachment;
	}

	public void setAttachment(Range attachment)
	{
		this.attachment = attachment;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [main=" + main + ", attachment=" + attachment + "]";
	}

	/**
	 * 数目范围。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class Range implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public static final String PROPERTY_MIN = "min";
		public static final String PROPERTY_MAX = "max";

		private Integer min = null;

		private Integer max = null;

		public Range()
		{
			super();
		}

		public Range(Integer min, Integer max)
		{
			super();
			this.min = min;
			this.max = max;
		}

		/**
		 * 返回最小值。
		 * 
		 * @return {@code null}表示不限
		 */
		public Integer getMin()
		{
			return min;
		}

		public void setMin(Integer min)
		{
			this.min = min;
		}

		/**
		 * 返回最大值。
		 * 
		 * @return {@code null}表示不限
		 */
		public Integer getMax()
		{
			return max;
		}

		public void setMax(Integer max)
		{
			this.max = max;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [min=" + min + ", max=" + max + "]";
		}
	}
}
