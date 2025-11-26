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

import org.datagear.analysis.ChartDefinition;
import org.datagear.analysis.RenderContext;

/**
 * {@linkplain RenderContext}属性名常量。
 * <p>
 * 注意：谨慎重构此类的常量值，因为它可能已被用于系统已创建的看板中，重构它将导致这些看板展示页面出错。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class RenderContextAttrs
{
	/**
	 * 内置属性名前缀。
	 * <p>
	 * 由于看板展示URL的请求参数会添加至渲染上下文属性中，为了避免名字冲突，所有内置属性名都应采用此前缀。
	 * </p>
	 */
	public static final String BUILTIN_ATTR_PREFIX = ChartDefinition.BUILTIN_ATTR_PREFIX;

	/** 看板主题 */
	public static final String DASHBOARD_THEME = BUILTIN_ATTR_PREFIX + "DASHBOARD_THEME";

	/** 图表主题 */
	public static final String CHART_THEME = BUILTIN_ATTR_PREFIX + "CHART_THEME";

	/** 当前用户 */
	public static final String USER = BUILTIN_ATTR_PREFIX + "USER";

	/** 地区 */
	public static final String LOCALE = BUILTIN_ATTR_PREFIX + "LOCALE";

	/** 应用根路径 */
	public static final String CONTEXT_PATH = BUILTIN_ATTR_PREFIX + "CONTEXT_PATH";

	/** 更新数据URL */
	public static final String UPDATE_DATA_URL = BUILTIN_ATTR_PREFIX + "UPDATE_DATA_URL";

	/** 加载图表URL */
	public static final String LOAD_CHART_URL = BUILTIN_ATTR_PREFIX + "LOAD_CHART_URL";

	/** 心跳URL */
	public static final String HEARTBEAT_URL = BUILTIN_ATTR_PREFIX + "HEARTBEAT_URL";

	/** 注销看板URL */
	public static final String UNLOAD_URL = BUILTIN_ATTR_PREFIX + "UNLOAD_URL";

	/** 插件资源URL前缀 */
	public static final String PLUGIN_RES_URL_PREFIX = BUILTIN_ATTR_PREFIX + "PLUGIN_RES_URL_PREFIX";

	/** 会话名 */
	public static final String SESSION_NAME = BUILTIN_ATTR_PREFIX + "SESSION_NAME";

	/** 会话值 */
	public static final String SESSION_VALUE = BUILTIN_ATTR_PREFIX + "SESSION_VALUE";
}
