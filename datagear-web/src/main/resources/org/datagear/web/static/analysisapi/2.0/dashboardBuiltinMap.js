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
 * 看板内置GeoJson地图
 * 
 * 加载时依赖：
 *   dashboardFactory.js
 * 
 * 运行时依赖:
 * 
 */
(function(global)
{

/**看板工厂*/
var DF = (global.dashboardFactory || (global.dashboardFactory = {}));

/**
 * 内置GeoJson地图配置：省级及以上。
 */
var builtinMaps =
[
	//世界地图
	{"name":["world", "世界"],"map":"world.json","adname":"世界","adcode":"world","parent":null},
	{
		"name":["100000","中国","中华人民共和国","china","China"],
		//标准中国地图南海诸岛太占空间，所以采用下面南海诸岛在右侧的中国地图
		//"map" : "100000_full.json",
		"map" : "china_nhzd.json",
		"adname":"中国","adcode":"100000","parent":null
	},
	{"name":["110000","北京市","北京","京","beijing","Beijing"],"map":"110000_full.json","adname":"北京市","adcode":"110000","parent":"100000"},
	{"name":["120000","天津市","天津","津","tianjin","Tianjin"],"map":"120000_full.json","adname":"天津市","adcode":"120000","parent":"100000"},
	{"name":["130000","河北省","河北","冀","hebei","Hebei"],"map":"130000_full.json","adname":"河北省","adcode":"130000","parent":"100000"},
	{"name":["140000","山西省","山西","晋","shanxi","Shanxi"],"map":"140000_full.json","adname":"山西省","adcode":"140000","parent":"100000"},
	{"name":["150000","内蒙古自治区","内蒙古","蒙","neimenggu","Neimenggu"],"map":"150000_full.json","adname":"内蒙古自治区","adcode":"150000","parent":"100000"},
	{"name":["210000","辽宁省","辽宁","辽","liaoning","Liaoning"],"map":"210000_full.json","adname":"辽宁省","adcode":"210000","parent":"100000"},
	{"name":["220000","吉林省","吉林","吉","jilin","Jilin"],"map":"220000_full.json","adname":"吉林省","adcode":"220000","parent":"100000"},
	{"name":["230000","黑龙江省","黑龙江","黑","heilongjiang","Heilongjiang"],"map":"230000_full.json","adname":"黑龙江省","adcode":"230000","parent":"100000"},
	{"name":["310000","上海市","上海","沪","shanghai","Shanghai"],"map":"310000_full.json","adname":"上海市","adcode":"310000","parent":"100000"},
	{"name":["320000","江苏省","江苏","苏","jiangsu","Jiangsu"],"map":"320000_full.json","adname":"江苏省","adcode":"320000","parent":"100000"},
	{"name":["330000","浙江省","浙江","浙","zhejiang","Zhejiang"],"map":"330000_full.json","adname":"浙江省","adcode":"330000","parent":"100000"},
	{"name":["340000","安徽省","安徽","皖","Anhui","anhui"],"map":"340000_full.json","adname":"安徽省","adcode":"340000","parent":"100000"},
	{"name":["350000","福建省","福建","闽","fujian","Fujian"],"map":"350000_full.json","adname":"福建省","adcode":"350000","parent":"100000"},
	{"name":["360000","江西省","江西","赣","jiangxi","Jiangxi"],"map":"360000_full.json","adname":"江西省","adcode":"360000","parent":"100000"},
	{"name":["370000","山东省","山东","鲁","shandong","Shandong"],"map":"370000_full.json","adname":"山东省","adcode":"370000","parent":"100000"},
	{"name":["410000","河南省","河南","豫","henan","Henan"],"map":"410000_full.json","adname":"河南省","adcode":"410000","parent":"100000"},
	{"name":["420000","湖北省","湖北","鄂","hubei","Hubei"],"map":"420000_full.json","adname":"湖北省","adcode":"420000","parent":"100000"},
	{"name":["430000","湖南省","湖南","湘","hunan","Hunan"],"map":"430000_full.json","adname":"湖南省","adcode":"430000","parent":"100000"},
	{"name":["440000","广东省","广东","粤","guangdong","Guangdong"],"map":"440000_full.json","adname":"广东省","adcode":"440000","parent":"100000"},
	{"name":["450000","广西壮族自治区","广西","桂","guangxi","Guangxi"],"map":"450000_full.json","adname":"广西壮族自治区","adcode":"450000","parent":"100000"},
	{"name":["460000","海南省","海南","琼","hainan","Hainan"],"map":"460000_full.json","adname":"海南省","adcode":"460000","parent":"100000"},
	{"name":["500000","重庆市","重庆","渝","chongqing","Chongqing"],"map":"500000_full.json","adname":"重庆市","adcode":"500000","parent":"100000"},
	{"name":["510000","四川省","四川","川","sichuan","Sichuan"],"map":"510000_full.json","adname":"四川省","adcode":"510000","parent":"100000"},
	{"name":["520000","贵州省","贵州","黔","guizhou","Guizhou"],"map":"520000_full.json","adname":"贵州省","adcode":"520000","parent":"100000"},
	{"name":["530000","云南省","云南","滇","yunnan","Yunnan"],"map":"530000_full.json","adname":"云南省","adcode":"530000","parent":"100000"},
	{"name":["540000","西藏自治区","西藏","藏","xizang","Xizang"],"map":"540000_full.json","adname":"西藏自治区","adcode":"540000","parent":"100000"},
	{"name":["610000","陕西省","陕西","陕","shanxi1","shaanxi","Shaanxi"],"map":"610000_full.json","adname":"陕西省","adcode":"610000","parent":"100000"},
	{"name":["620000","甘肃省","甘肃","甘","gansu","Gansu"],"map":"620000_full.json","adname":"甘肃省","adcode":"620000","parent":"100000"},
	{"name":["630000","青海省","青海","青","qinghai","Qinghai"],"map":"630000_full.json","adname":"青海省","adcode":"630000","parent":"100000"},
	{"name":["640000","宁夏回族自治区","宁夏","宁","ningxia","Ningxia"],"map":"640000_full.json","adname":"宁夏回族自治区","adcode":"640000","parent":"100000"},
	{"name":["650000","新疆维吾尔自治区","新疆","新","xinjiang","Xinjiang"],"map":"650000_full.json","adname":"新疆维吾尔自治区","adcode":"650000","parent":"100000"},
	{"name":["710000","台湾省","台湾","taiwan","Taiwan"],"map":"710000.json","adname":"台湾省","adcode":"710000","parent":"100000"},
	{"name":["810000","香港特别行政区","香港","港","xianggang","Xianggang","HongKong","Hongkong"],"map":"810000_full.json","adname":"香港特别行政区","adcode":"810000","parent":"100000"},
	{"name":["820000","澳门特别行政区","澳门","澳","aomen","Aomen","Macao"],"map":"820000_full.json","adname":"澳门特别行政区","adcode":"820000","parent":"100000"}
];

DF.registerBuiltinMap(builtinMaps);

})(this);