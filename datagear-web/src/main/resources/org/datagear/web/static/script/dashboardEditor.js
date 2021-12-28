/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 看板可视编辑器。
 * 全局变量名：window.dashboardFactory.dashboardEditor
 * 
 * 加载时依赖：
 *   无
 * 
 * 运行时依赖:
 *   jquery.js
 *   dashboardFactory.js
 */
(function(global)
{
	/**看板工厂*/
	var dashboardFactory = (global.dashboardFactory || (global.dashboardFactory = {}));
	var editor = (dashboardFactory.dashboardEditor || (dashboardFactory.dashboardEditor = {}));
	
	editor.evalTopWindowSize = function()
	{
		var topWindow = window;
		while(topWindow.parent  && topWindow.parent != topWindow)
			topWindow = topWindow.parent;
		
		var size =
		{
			width: $(topWindow).width(),
			height: $(topWindow).height()
		};
		
		return size;
	};
	
})
(this);