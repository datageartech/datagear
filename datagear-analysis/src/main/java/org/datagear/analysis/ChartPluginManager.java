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

package org.datagear.analysis;

import java.util.List;

/**
 * {@linkplain ChartPlugin}管理器。
 * 
 * @author datagear@163.com
 *
 */
public interface ChartPluginManager
{
	/**
	 * 是否可成功注册。
	 * 
	 * @param chartPlugin
	 * @return {@code true} 是；{@code false} 否，比如版本号不够新
	 */
	boolean isRegisterable(ChartPlugin chartPlugin);

	/**
	 * 注册一个{@linkplain ChartPlugin}。
	 * 
	 * @param chartPlugin
	 * @return {@code true} 注册成功；{@code false} 注册失败，比如版本号不够新
	 */
	boolean register(ChartPlugin chartPlugin);

	/**
	 * 移除指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param ids
	 * @return 被移除的{@linkplain ChartPlugin}或者{@code null}。
	 */
	ChartPlugin[] remove(String... ids);

	/**
	 * 获取指定ID的{@linkplain ChartPlugin}。
	 * 
	 * @param id
	 * @return
	 */
	ChartPlugin get(String id);

	/**
	 * 获取指定类型的所有{@linkplain ChartPlugin}。
	 * <p>
	 * 返回结果将根据{@linkplain ChartPlugin#getOrder()}进行排序，越小越靠前。
	 * </p>
	 * 
	 * @param chartPluginType
	 * @return
	 */
	<T extends ChartPlugin> List<T> getAll(Class<? super T> chartPluginType);

	/**
	 * 获取所有{@linkplain ChartPlugin}。
	 * <p>
	 * 返回结果将根据{@linkplain ChartPlugin#getOrder()}进行排序，越小越靠前。
	 * </p>
	 * 
	 * @return
	 */
	List<ChartPlugin> getAll();
}
