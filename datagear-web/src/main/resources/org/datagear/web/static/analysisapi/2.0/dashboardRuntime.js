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

/**
 * 看板运行环境，封装对外开放的看板运行环境API。
 * 全局变量名：window.dashboardRuntime
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖：
 *   chartFactory
 *   dashboardFactory
 */
(function(global)
{

var DR = (global.dashboardRuntime || (global.dashboardRuntime = {}));

/**
 * 注册全局地图。
 */
DR.registerMap = function()
{
	
};

/**
 * 注册全局图表渲染器依赖库。
 * 
 * @param lib 全局依赖库对象、数组，格式同chartFactory.registerGlobalLib()函数的lib参数
 */
DR.registerLib = function(lib)
{
	global.chartFactory.registerGlobalLib(lib);
};

DR.registerLibStore = function()
{
	
};

})(this);
