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
 *   chartFactory.js
 *   dashboardFactory.js
 */
(function(global)
{

var CF = (global.chartFactory || (global.chartFactory = {}));
var DF = (global.dashboardFactory || (global.dashboardFactory = {}));
var builtinMapBaseURL = (DF.builtinMapBaseURL || (DF.builtinMapBaseURL = "/static/analysislib/geojson/"));

/**
 * 注册内置地图信息。
 * 
 * @param mapURLInfos { name: "..."、[ "...", ... ],value:{ url: "...", ... } }、[ { ... }, ... ]
 */
DF.registerBuiltinMapURL = function(mapURLInfos)
{
	mapURLInfos = (CF.isArray(mapURLInfos) ? mapURLInfos : [ mapURLInfos ]);
	
	for(let i=0; i<mapURLInfos.length; i++)
	{
		let info = mapURLInfos[i];
		let name = info.name;
		let value = info.value;
		
		if(CF.isEmpty(name) || value == null || CF.isEmpty(value.url))
			continue;
		
		value.url = builtinMapBaseURL + value.url;
		
		//确保adcode也会注册
		if(!CF.isEmpty(value.adcode))
		{
			if(CF.isArray(name))
			{
				if(CF.indexInArray(name, value.adcode) < 0)
					name.push(value.adcode);
			}
			else
			{
				if(name !== value.adcode)
				{
					name = [ name ];
					name.push(value.adcode);
				}
			}
		}
		
		CF.registerMapURL(name, value);
	}
};

/**
 * 获取标准内置地图信息树形结构。
 * 返回一个数组，其中每个元素都可能是树形结构根节点，节点格式为：
 * {
 *   //地图名
 *   mapName: "...",
 *   //显示标签
 *   mapLabel: "...",
 *   //子节点，为null表示没有
 *   mapChildren: [ ... ],
 * }
 * 
 * @param listener 可选，节点监听器，格式为：
 * {
 *   //节点添加后置处理函数，parent为null表明节点添加到了rootArray中
 *   added: function(node, parent, rootArray){}
 * }
 */
DF.getStdBuiltinMapTree = function(listener)
{
	var re = [];
	
	var bms = DF.resolveStdBuiltinMapInfo();
	var nodeCache = {};
	
	for(let i=0; i<bms.length; i++)
	{
		let bm = bms[i];
		
		if(CF.isEmpty(bm.adname) || CF.isEmpty(bm.adcode))
			continue;
		
		//DF.registerBuiltinMapURL()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bm.adcode, mapLabel: bm.adname };
		let parentNode = (bm.parent ? nodeCache[bm.parent] : null);
		let addTo = re;
		
		if(parentNode)
		{
			if(!parentNode.mapChildren)
				parentNode.mapChildren = [];
			
			addTo = parentNode.mapChildren;
		}
		
		//删除旧的
		let existIdx = CF.indexInArrayOfProp(addTo, node.mapName, "mapName");
		if(existIdx >= 0)
			addTo.splice(existIdx, 1);
		
		addTo.push(node);
		
		if(listener && listener.added)
			listener.added(node, parentNode, re);
		
		nodeCache[bm.adcode] = node;
	}
	
	return re;
};

/**
 * 获取标准内置图表地图平铺数组。
 * 返回一个数组，其中元素格式为：
 * {
 *   //地图名
 *   mapName: "...",
 *   //显示标签
 *   mapLabel: "..."
 * }
 * 
 * @param listener 可选，节点监听器，格式为：
 * {
 *   //节点添加后置处理函数
 *   added: function(node, rootArray){}
 * }
 */
DF.getStdBuiltinMapArray = function(listener)
{
	var re = [];
	
	var bms = DF.resolveStdBuiltinMapInfo();
	
	for(let i=0; i<bms.length; i++)
	{
		let bm = bms[i];
		
		if(!bm.adname || !bm.adcode)
			continue;
		
		//DF.registerBuiltinMapURL()函数已经确保了adcode可以用作地图名
		//而且它是全局唯一的，最合适
		let node = { mapName: bm.adcode, mapLabel: bm.adname };
		
		//删除旧的
		let existIdx = CF.indexInArrayOfProp(re, node.mapName, "mapName");
		if(existIdx >= 0)
			re.splice(existIdx, 1);
		
		re.push(node);
		
		if(listener && listener.added)
			listener.added(node, re);
	}
	
	return re;
};

DF.resolveStdBuiltinMapInfo = function()
{
	var re = [];
	
	var values = (CF.mapHandler.values || {});
	
	for(let name in values)
	{
		let value = values[name];
		
		if(!CF.isEmpty(value.adcode) && name === value.adcode)
			re.push(value);
	}
	
	re.sort(function(a, b)
	{
		if(a.adcode > b.adcode)
			return 1;
		else if(a.adcode < b.adcode)
			return -1;
		else
			return 0;
	});
	
	return re;
};

/**
 * 内置GeoJson地图配置：省级及以上。
 */
var builtinMaps =
[
	//世界地图
	{name:["world", "世界"],value:{"url":"world.json","adname":"世界","adcode":"world","parent":null}},
	{
		name:["100000","中国","中华人民共和国","china","China"],
		//标准中国地图南海诸岛太占空间，所以采用下面南海诸岛在右侧的中国地图
		//"url" : "100000_full.json",
		value: {"url":"china_nhzd.json","adname":"中国","adcode":"100000","parent":null}
	},
	{name:["110000","北京市","北京","京","beijing","Beijing"],value:{"url":"110000_full.json","adname":"北京市","adcode":"110000","parent":"100000"}},
	{name:["120000","天津市","天津","津","tianjin","Tianjin"],value:{"url":"120000_full.json","adname":"天津市","adcode":"120000","parent":"100000"}},
	{name:["130000","河北省","河北","冀","hebei","Hebei"],value:{"url":"130000_full.json","adname":"河北省","adcode":"130000","parent":"100000"}},
	{name:["140000","山西省","山西","晋","shanxi","Shanxi"],value:{"url":"140000_full.json","adname":"山西省","adcode":"140000","parent":"100000"}},
	{name:["150000","内蒙古自治区","内蒙古","蒙","neimenggu","Neimenggu"],value:{"url":"150000_full.json","adname":"内蒙古自治区","adcode":"150000","parent":"100000"}},
	{name:["210000","辽宁省","辽宁","辽","liaoning","Liaoning"],value:{"url":"210000_full.json","adname":"辽宁省","adcode":"210000","parent":"100000"}},
	{name:["220000","吉林省","吉林","吉","jilin","Jilin"],value:{"url":"220000_full.json","adname":"吉林省","adcode":"220000","parent":"100000"}},
	{name:["230000","黑龙江省","黑龙江","黑","heilongjiang","Heilongjiang"],value:{"url":"230000_full.json","adname":"黑龙江省","adcode":"230000","parent":"100000"}},
	{name:["310000","上海市","上海","沪","shanghai","Shanghai"],value:{"url":"310000_full.json","adname":"上海市","adcode":"310000","parent":"100000"}},
	{name:["320000","江苏省","江苏","苏","jiangsu","Jiangsu"],value:{"url":"320000_full.json","adname":"江苏省","adcode":"320000","parent":"100000"}},
	{name:["330000","浙江省","浙江","浙","zhejiang","Zhejiang"],value:{"url":"330000_full.json","adname":"浙江省","adcode":"330000","parent":"100000"}},
	{name:["340000","安徽省","安徽","皖","Anhui","anhui"],value:{"url":"340000_full.json","adname":"安徽省","adcode":"340000","parent":"100000"}},
	{name:["350000","福建省","福建","闽","fujian","Fujian"],value:{"url":"350000_full.json","adname":"福建省","adcode":"350000","parent":"100000"}},
	{name:["360000","江西省","江西","赣","jiangxi","Jiangxi"],value:{"url":"360000_full.json","adname":"江西省","adcode":"360000","parent":"100000"}},
	{name:["370000","山东省","山东","鲁","shandong","Shandong"],value:{"url":"370000_full.json","adname":"山东省","adcode":"370000","parent":"100000"}},
	{name:["410000","河南省","河南","豫","henan","Henan"],value:{"url":"410000_full.json","adname":"河南省","adcode":"410000","parent":"100000"}},
	{name:["420000","湖北省","湖北","鄂","hubei","Hubei"],value:{"url":"420000_full.json","adname":"湖北省","adcode":"420000","parent":"100000"}},
	{name:["430000","湖南省","湖南","湘","hunan","Hunan"],value:{"url":"430000_full.json","adname":"湖南省","adcode":"430000","parent":"100000"}},
	{name:["440000","广东省","广东","粤","guangdong","Guangdong"],value:{"url":"440000_full.json","adname":"广东省","adcode":"440000","parent":"100000"}},
	{name:["450000","广西壮族自治区","广西","桂","guangxi","Guangxi"],value:{"url":"450000_full.json","adname":"广西壮族自治区","adcode":"450000","parent":"100000"}},
	{name:["460000","海南省","海南","琼","hainan","Hainan"],value:{"url":"460000_full.json","adname":"海南省","adcode":"460000","parent":"100000"}},
	{name:["500000","重庆市","重庆","渝","chongqing","Chongqing"],value:{"url":"500000_full.json","adname":"重庆市","adcode":"500000","parent":"100000"}},
	{name:["510000","四川省","四川","川","sichuan","Sichuan"],value:{"url":"510000_full.json","adname":"四川省","adcode":"510000","parent":"100000"}},
	{name:["520000","贵州省","贵州","黔","guizhou","Guizhou"],value:{"url":"520000_full.json","adname":"贵州省","adcode":"520000","parent":"100000"}},
	{name:["530000","云南省","云南","滇","yunnan","Yunnan"],value:{"url":"530000_full.json","adname":"云南省","adcode":"530000","parent":"100000"}},
	{name:["540000","西藏自治区","西藏","藏","xizang","Xizang"],value:{"url":"540000_full.json","adname":"西藏自治区","adcode":"540000","parent":"100000"}},
	{name:["610000","陕西省","陕西","陕","shanxi1","shaanxi","Shaanxi"],value:{"url":"610000_full.json","adname":"陕西省","adcode":"610000","parent":"100000"}},
	{name:["620000","甘肃省","甘肃","甘","gansu","Gansu"],value:{"url":"620000_full.json","adname":"甘肃省","adcode":"620000","parent":"100000"}},
	{name:["630000","青海省","青海","青","qinghai","Qinghai"],value:{"url":"630000_full.json","adname":"青海省","adcode":"630000","parent":"100000"}},
	{name:["640000","宁夏回族自治区","宁夏","宁","ningxia","Ningxia"],value:{"url":"640000_full.json","adname":"宁夏回族自治区","adcode":"640000","parent":"100000"}},
	{name:["650000","新疆维吾尔自治区","新疆","新","xinjiang","Xinjiang"],value:{"url":"650000_full.json","adname":"新疆维吾尔自治区","adcode":"650000","parent":"100000"}},
	{name:["710000","台湾省","台湾","taiwan","Taiwan"],value:{"url":"710000.json","adname":"台湾省","adcode":"710000","parent":"100000"}},
	{name:["810000","香港特别行政区","香港","港","xianggang","Xianggang","HongKong","Hongkong"],value:{"url":"810000_full.json","adname":"香港特别行政区","adcode":"810000","parent":"100000"}},
	{name:["820000","澳门特别行政区","澳门","澳","aomen","Aomen","Macao"],value:{"url":"820000_full.json","adname":"澳门特别行政区","adcode":"820000","parent":"100000"}}
];

DF.registerBuiltinMapURL(builtinMaps);

})(this);