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

package org.datagear.web.analysis;

import org.datagear.analysis.ChartResult;
import org.datagear.analysis.DataSetResult;

/**
 * 看板版本。
 * <p>
 * 在系统{@code 5.3.0}版本起，有以下不兼容的改动：
 * </p>
 * <p>
 * 1、内置看板资源JS库中，移除了表格、水球图、词云图的依赖JS库，改为由插件依赖库实现，按需引入；
 * </p>
 * <p>
 * 2、图表渲染器的{@code update(chart, results)}、图表监听器的{@code update(chart, results)}、{@code onUpdate(chart, results)}的{@code results}参数名改为{@code chartResult}，
 * 类型由数组（{@linkplain DataSetResult}格式的数组）改为对象（{@linkplain ChartResult}对象）。
 * </p>
 * <p>
 * 为了兼容旧版本，需通过看板版本号区分处理。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DashboardVersion
{
	/**
	 * 版本：{@code 1.0}
	 * <p>
	 * 用于标识系统{@code 5.2.0}及更低版本的看板。
	 * </p>
	 */
	public static final String V_1_0 = "1.0";

	/**
	 * 版本：{@code 2.0}
	 * <p>
	 * 用于标识系统{@code 5.3.0}及更高版本的看板。
	 * </p>
	 */
	public static final String V_2_0 = "2.0";

	/**
	 * 是否{@linkplain #V_1_0}版本。
	 * 
	 * @param version
	 * @return
	 */
	public static boolean isVersion_1_0(String version)
	{
		return V_1_0.equals(version);
	}
}
