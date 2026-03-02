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
 * 全局图表依赖库注册器
 * 
 * 加载时依赖：
 *   chartFactory
 */
(function(global)
{

var CF = global.chartFactory;

CF.registerGlobalLib(
[
	{
		name: "echarts",
		version: "6.0.0",
		source: "/static/analysislib/echarts-6.0.0/echarts.min.js"
	},
	{
		name: "echarts",
		version: "5.6.0",
		source: "/static/analysislib/echarts-5.6.0/echarts.min.js"
	},
	//新发布的jQuery-4.0.0并未明确其支持的浏览器最低版本，所以暂不引入
	{
		name: "jQuery",
		version: "3.7.1",
		source: "/static/analysislib/jquery-3.7.1/jquery-3.7.1.min.js"
	}
]);

})(this);
