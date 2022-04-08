<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "../include/import_global.ftl">
<#include "../include/html_doctype.ftl">
<#--
preview 是否是预览请求，允许为null
-->
<#assign preview=(preview!false)>
<html>
<head>
<#include "../include/html_head.ftl">
<title><#include "../include/html_title_app_name.ftl"><@spring.message code='schema.schemaBuildUrl' /></title>
</head>
<body>
<#include "../include/page_js_obj.ftl" >
<div id="${pageId}" class="page-form page-form-buildSchemaUrl">
	<div id="dbUrlBuilderScriptCode" style="display: none;">
		${scriptCode!''}
	</div>
	<div class="builtInBuildersJson" style="display: none;">
		${builtInBuildersJson!''}
	</div>
	<form id="${pageId}form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.url.dbType' /></label>
				</div>
				<div class="form-item-value">
					<select name="dbType">
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.url.host' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="host" value="" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.url.port' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="port" value="" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schema.url.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="" class="ui-widget ui-widget-content ui-corner-all" />
				</div>
			</div>
		</div>
		<div class="form-foot">
			<input type="submit" value="<@spring.message code='confirm' />" class="recommended" />
		</div>
	</form>
	<#if preview>
	<div class="url-preview"></div>
	</#if>
</div>
<#include "../include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	po.dbTypeSelect = po.element("select[name='dbType']");
	
	po.initUrl = "${(url!'')?js_string?no_esc}";
	
	$.schemaUrlBuilder.clear();
	
	po.initBuilderSelect = function()
	{
		var customBuilders = [];
		var buildInBuilders = [];
		
		try
		{
			var scriptCode = po.element("#dbUrlBuilderScriptCode").text();
			var builtInBuildersJson = po.element(".builtInBuildersJson").text();
			
			if(builtInBuildersJson)
				buildInBuilders = eval("$.schemaUrlBuilder.add(" + builtInBuildersJson+");");
			
			if(scriptCode)
				customBuilders = eval("$.schemaUrlBuilder.add(" + scriptCode+");");
		}
		catch(e)
		{
			$.tipError("<@spring.message code='schema.loadUrlBuilderScriptError' /><@spring.message code='colon' />" + e.message);
		}
		
		if(customBuilders.length <= 0)
			customBuilders = (buildInBuilders.length <= 3 ? buildInBuilders : buildInBuilders.slice(0, 3));
		
		if(customBuilders.length > 0)
		{
			var optgrp = $("<optgroup />").attr("label", "<@spring.message code='schema.schemaBuildUrl.common' />")
				.appendTo(po.dbTypeSelect);
			for(var i=0; i<customBuilders.length; i++)
			{
				var builder = customBuilders[i];
				$("<option />").attr("value", builder.dbType).attr("title", builder.dbType)
					.html((builder.dbDesc || builder.dbType)).appendTo(optgrp);
			}
		}
		
		var allBuilders = buildInBuilders.concat([]);
	
		for(var i=0; i<customBuilders.length; i++)
		{
			var builder = customBuilders[i];
			var contains = false;
			
			for(var j=0; j<buildInBuilders.length; j++)
			{
				if(builder.dbType == buildInBuilders[j].dbType)
				{
					contains=true;
					break;
				}
			}
			
			if(!contains)
				allBuilders.push(builder);
		}
		
		if(allBuilders.length > 0)
		{
			allBuilders.sort(function(ba, bb)
			{
				if(ba.dbType < bb.dbType)
					return -1;
				else if(ba.dbType > bb.dbType)
					return 1;
				else
					return 0;
			});
			
			var optgrp = $("<optgroup />").attr("label", "<@spring.message code='schema.schemaBuildUrl.all' />")
				.appendTo(po.dbTypeSelect);
			for(var i=0; i<allBuilders.length; i++)
			{
				var builder = allBuilders[i];
				$("<option />").attr("value", builder.dbType).attr("title", builder.dbType)
					.html((builder.dbDesc || builder.dbType)).appendTo(optgrp);
			}
		}
		
		po.dbTypeSelect.selectmenu(
		{
			"classes" :
			{
				"ui-selectmenu-button" : "schema-build-url-dbtype-select ",
				"ui-selectmenu-menu" : "schema-build-url-dbtype-selectmenu ui-widget ui-widget-content ui-corner-all ui-widget-shadow"
			},
			change : function(event, ui)
			{
				var dbType = ui.item.value;
				
				var defaultUrlInfo = $.schemaUrlBuilder.defaultValue(dbType);
				po.setFormUrlValue(defaultUrlInfo);
			},
			open: function(event, ui)
			{
				$(".schema-build-url-dbtype-selectmenu .ui-menu").css("width", "100%");
			}
		});
	}
	
	po.setFormUrlValue = function(value)
	{
		if(!value)
			return;
		
		for(var name in value)
		{
			var inputValue = value[name];
			
			if(inputValue)
				po.element("input[name='"+name+"']").val(inputValue);
		}
	};
	
	po.buildFormUrl = function()
	{
		var dbType = po.dbTypeSelect.val();
		
		var value = {};
		
		var inputs = po.element("input[type='text']");
		for(var i=0; i<inputs.length; i++)
		{
			var input = $(inputs[i]);
			value[input.attr("name")] = input.val();
		}
		
		return $.schemaUrlBuilder.build(dbType, value);
	};
	
	po.validateForm(
	{
		submitHandler : function(form)
		{
			var url = po.buildFormUrl();
			
			<#if preview>
			po.element(".url-preview").text(url);
			<#else>
			if(po.pageParamCall("setSchemaUrl", url)  != false);
				po.close();
			</#if>
			
			return false;
		}
	});
	
	po.initBuilderSelect();
	
	var initUrlValue = undefined;
	
	if(po.initUrl)
	{
		var urlInfo = $.schemaUrlBuilder.extract(po.initUrl);
		
		if(urlInfo != null)
		{
			po.dbTypeSelect.val(urlInfo.dbType);
			po.dbTypeSelect.selectmenu("refresh");
			initUrlValue = urlInfo.value;
		}
	}
	
	if(!initUrlValue)
		initUrlValue = $.schemaUrlBuilder.defaultValue(po.dbTypeSelect.val());
	
	po.setFormUrlValue(initUrlValue);
})
(${pageId});
</script>
</body>
</html>