<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<script type="text/javascript">
$.schemaUrlBuilder.clear();
try
{
$.schemaUrlBuilder.add(
<%=request.getAttribute("scriptCode")%>
);
}
catch(e)
{
	$.tipError("<fmt:message key='schema.loadUrlBuilderScriptError' />");
}
</script>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='schema.schemaBuildUrl' /></title>
</head>
<body>
<div id="${pageId}" class="schema-build-url-form">
	<form id="${pageId}-form" action="#" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.url.dbName' /></label>
				</div>
				<div class="form-item-value">
					<select name="dbName">
					</select>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.url.host' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="host" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.url.port' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="port" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.url.name' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="name" value="" class="ui-widget ui-widget-content" />
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<fmt:message key='confirm' />" class="recommended" />
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	pageObj.dbNameSelect = pageObj.element("select[name='dbName']");

	var pageParam = pageObj.pageParam();
	
	pageObj.initUrl = (pageParam && pageParam.initUrl ? pageParam.initUrl : "");
	
	$("input:submit, input:button, input:reset, button", pageObj.page).button();
	
	var builderInfos = $.schemaUrlBuilder.list();
	for(var i=0; i<builderInfos.length; i++)
	{
		var builderInfo = builderInfos[i];
		$("<option>").attr("value", builderInfo.dbName).html(builderInfo.dbDesc).appendTo(pageObj.dbNameSelect);
	}
	
	pageObj.dbNameSelect.selectmenu(
	{
		"classes" : { "ui-selectmenu-button" : "schema-build-url-dbtype-select" },
		change : function(event, ui)
		{
			var dbName = ui.item.value;
			
			var defaultUrlInfo = $.schemaUrlBuilder.defaultValue(dbName);
			pageObj.setFormUrlValue(defaultUrlInfo);
		}
	});
	
	pageObj.setFormUrlValue = function(value)
	{
		if(!value)
			return;
		
		for(var name in value)
		{
			var inputValue = value[name];
			
			if(inputValue)
				pageObj.element("input[name='"+name+"']").val(inputValue);
		}
	};
	
	pageObj.buildFormUrl = function()
	{
		var dbName = pageObj.dbNameSelect.val();
		
		var value = {};
		
		var inputs = pageObj.element("input[type='text']");
		for(var i=0; i<inputs.length; i++)
		{
			var input = $(inputs[i]);
			value[input.attr("name")] = input.val();
		}
		
		return $.schemaUrlBuilder.build(dbName, value);
	};
	
	pageObj.form.validate(
	{
		submitHandler : function(form)
		{
			var pageParam = pageObj.pageParam();
			
			var close = true;
			
			if(pageParam && pageParam.setSchemaUrl)
				close = (pageParam.setSchemaUrl(pageObj.buildFormUrl()) != false);
			
			if(close)
				pageObj.close();
			
			return false;
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item"));
		}
	});
	
	var initUrlValue = undefined;
	
	if(pageObj.initUrl)
	{
		var urlInfo = $.schemaUrlBuilder.extract(pageObj.initUrl);
		
		if(urlInfo != null)
		{
			pageObj.dbNameSelect.val(urlInfo.dbName);
			pageObj.dbNameSelect.selectmenu("refresh");
			initUrlValue = urlInfo.value;
		}
	}
	
	if(!initUrlValue)
		initUrlValue = $.schemaUrlBuilder.defaultValue(pageObj.dbNameSelect.val());
	
	pageObj.setFormUrlValue(initUrlValue);
})
(${pageId});
</script>
</body>
</html>