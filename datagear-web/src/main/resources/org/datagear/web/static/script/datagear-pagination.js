/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

/**
 * 分页组件。
 * 依赖:
 * jquery.js
 * jquery-ui.js
 * jquery.cookie.js
 */
(function($, undefined)
{
	$.widget("datagear.pagination",
	{
		options:
		{
			//可选，页码
			page: 1,
			
			//可选，页大小
			pageSize : 10,
			
			//可选，总记录数
			total: 0,
			
			//可选，页大小选项
			pageSizeOptions : [[5, 10], [20, 50], [100, 200], [500, 1000]],
			
			//可选，页大小cookie名
			pageSizeCookie : "pagination.pageSize",

			//可选，页大小cookie路径
			pageSizeCookiePath : "/",
			
			pageSizeSetLabel : "确定",
			
			//可选，更新回调函数
			//@param page 待更新的页码
			//@param pageSize 待更新的页大小
			//@param total 待更新的总记录数
			//@return false，不更新组件；true，更新组件
			update : function(page, pageSize, total){},
			
			//可选，标签模版
			labelTemplate : "<span class='ui-state-disabled'>共<span class='label-rt'>-</span>条，</span>"
							+"<span class='ui-state-disabled'>每页<span class='label-ps'>-</span>条</span><span class='ui-icon ui-icon-triangle-1-e label-pss'></span>"
							+"<span class='ui-state-disabled'><span class='label-cp'>-</span>/<span class='label-tp'>-</span></span>",
			
			//可选，跳转页按钮标签
			toPageLabel : "跳转"
		},
		
		_create: function()
		{
			this.element.addClass("pagination");
			
			if(this.options.pageSizeCookie)
				this.options.pageSize = ($.cookie(this.options.pageSizeCookie) || this.options.pageSize);
			
			var thisWidget = this;
			
			var label = $("<div class='label' title='' />").html(this.options.labelTemplate).appendTo(this.element);
			var pss = $("<div class='ui-widget ui-widget-content ui-corner-all page-size-set' style='position: absolute; top: 0px; left: 0px;' />").appendTo(label);
			
			this._createPageSizeSetConent(pss, this.options.pageSizeOptions);
			
			pss.hover(function(){},function(){ $(this).hide(); }).hide();
			
			$(".label-pss", label).click(function()
			{
				var pss = $(".page-size-set", thisWidget.element);
				
				if(pss.is(":hidden"))
				{
					$("td", pss).each(function()
					{
						var _this = $(this);
						var text = _this.text();
						
						if(thisWidget.options.pageSize+"" == text)
							_this.addClass("ui-state-active");
						else
							_this.removeClass("ui-state-active");
					});
					pss.show().position({my:"center bottom", at:"center top", of:$(this)});
				}
				else
					pss.hide();
			});
			
			var firstbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-first' />").html("<span class='ui-icon ui-icon-seek-first'></span>").appendTo(this.element).button();
			var prevbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-prev' />").html("<span class='ui-icon ui-icon-seek-prev'></span>").appendTo(this.element).button();
			var nextbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-next' />").html("<span class='ui-icon ui-icon-seek-next'></span>").appendTo(this.element).button();
			var endbtn = $("<div class='ui-state-default ui-corner-all page page-icon page-end' />").html("<span class='ui-icon ui-icon-seek-end'></span>").appendTo(this.element).button();
			
			firstbtn.click(function()
			{
				thisWidget._updateCallback(1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			prevbtn.click(function()
			{
				thisWidget._updateCallback(thisWidget.options.page - 1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			nextbtn.button().click(function()
			{
				thisWidget._updateCallback(thisWidget.options.page + 1);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			endbtn.button().click(function()
			{
				var pages =  ( thisWidget.options.pageSize < 1 ? 0 : Math.ceil(thisWidget.options.total/thisWidget.options.pageSize));
				
				thisWidget._updateCallback(pages);
			})
			.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
			
			var topagediv = $("<div class='to-page' />").appendTo(this.element);
			var topageform = $("<form class='ui-widget ui-widget-content ui-corner-all' />").appendTo(topagediv);
			$("<input type='text' name='topage' class='ui-widget ui-widget-content' />").appendTo(topageform);
			$("<input type='submit' />").val(this.options.toPageLabel).appendTo(topageform).button();
			
			topageform.submit(function()
			{
				var page = $("input[name='topage']", this).val();
				
				thisWidget._updateCallback(page);
				
				return false;
			});
			
			this._render();
		},
		
		/**
		 * 创建页大小设置内容。
		 */
		_createPageSizeSetConent: function($parent, pageSizeOptions)
		{
			var thisWidget = this;
			
			var maxCols = 1;
			
			for(var i=0; i<pageSizeOptions.length; i++)
			{
				var ele = pageSizeOptions[i];
				if(ele.length && maxCols < ele.length)
					maxCols = ele.length;
			}
			
			var table = $("<table />").appendTo($parent);
			
			for(var i=0; i<pageSizeOptions.length; i++)
			{
				var tr = $("<tr />").appendTo(table);
				
				var ele = pageSizeOptions[i];
				
				for(var j=0; j<maxCols; j++)
				{
					if(ele.length)
					{
						if(j < ele.length)
						{
							$("<td />").html(ele[j]).appendTo(tr)
							.click(function(){ thisWidget._updateCallback(null, $(this).text()); $parent.hide(); })
							.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); });
						}
						else
							$("<td />").appendTo(tr);
					}
					else
					{
						if(j == 0)
						{
							$("<td />").html(ele).appendTo(tr)
							.click(function(){ thisWidget._updateCallback(null, $(this).text()); $parent.hide(); })
							.hover(function(){ $(this).addClass("ui-state-hover"); }, function(){ $(this).removeClass("ui-state-hover"); })
						}
						else
							$("<td />").appendTo(tr);
					}
				}
			}
			
			var customdiv = $("<div class='page-size-custom' />").appendTo($parent);
			var customform = $("<form class='ui-widget ui-widget-content ui-corner-all' />").appendTo(customdiv);
			$("<input type='text' name='pageSize' class='ui-widget ui-widget-content' />").appendTo(customform);
			$("<input type='submit' />").val(this.options.pageSizeSetLabel).appendTo(customform).button();
			customform.submit(function()
			{
				var pageSize = $("input[name='pageSize']", this).val();
				
				thisWidget._updateCallback(null, pageSize);
				$parent.hide();
				
				return false;
			});
		},
		
		_destroy: function()
		{
			
		},
		
		_setOption: function(key, value)
		{
			if ( key === "page" )
				value = this._formatPage(value);
			else if(key == 'pageSize')
			{
				if(this.options.pageSizeCookie)
					$.cookie(this.options.pageSizeCookie, value, { expires : 365*5, path: this.options.pageSizeCookiePath });
			}
			
			this._super(key, value);
		},
		
		_setPageOption: function(page)
		{
			this._setOption("page", page);
		},
		
		_setPageSizeOption: function(pageSize)
		{
			this._setOption("pageSize", pageSize);
		},
		
		/**
		 * 刷新。
		 */
		refresh : function()
		{
			this._render();
		},
		
		/**
		 * 更新处理函数。
		 * @param page
		 * @param pageSize
		 * @param total
		 */
		_updateCallback : function(page, pageSize, total)
		{
			if(!page)
				page = this.options.page;
			if(!pageSize)
				pageSize = this.options.pageSize;
			if(!total)
				total = this.options.total;
			
			page = this._formatPage(page);
			
			var update = this.options.update.call(this, page, pageSize, total);
			
			if(update != false)
			{
				this._setPageOption(page);
				this._setPageSizeOption(pageSize);
				this._setOption("total", total);
				
				this._render();
			}
		},
	
		/**
		 * 绘制。
		 */
		_render : function()
		{
			var thisWidget = this;
			
			var page = this.options.page;
			var total = this.options.total;
			var pageSize = this.options.pageSize;
			
			var pages =  ( pageSize < 1 ? 0 : Math.ceil(total/pageSize));
			
			var label = $(".label", this.element);
			$(".label-rt", label).text(total);
			$(".label-ps", label).text(pageSize);
			$(".label-cp", label).text(page);
			$(".label-tp", label).text(pages);
			
			var firstbtn = $(".page-first", this.element);
			var predbtn = $(".page-prev", this.element);
			var nextbtn = $(".page-next", this.element);
			var endbtn = $(".page-end", this.element);
			
			if(page > 1)
			{
				firstbtn.button( "enable" );
				predbtn.button( "enable" );
			}
			else
			{
				firstbtn.button( "disable" );
				predbtn.button( "disable" );
			}
			
			if(page < pages)
			{
				nextbtn.button( "enable" );
				endbtn.button( "enable" );
			}
			else
			{
				nextbtn.button( "disable" );
				endbtn.button( "disable" );
			}
		},
		
		/**
		 * 格式化页码。
		 */
		_formatPage : function(page)
		{
			var total = this.options.total;
			var pageSize = this.options.pageSize;
			
			page = (typeof(page) == "string" ? parseInt(page) : page);
			var pages =  ( pageSize < 1 ? 0 : Math.ceil(total/pageSize));
			
			if(page < 1 || isNaN(page))
				page = 1;
			
			if(page > pages)
				page = pages;
			
			return page;
		}
	});
})
(jQuery);
