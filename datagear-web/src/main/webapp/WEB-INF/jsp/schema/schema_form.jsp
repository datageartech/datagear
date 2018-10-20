<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.datagear.web.controller.SchemaController" %>
<%@ include file="../include/jsp_import.jsp" %>
<%@ include file="../include/jsp_ajax_request.jsp" %>
<%@ include file="../include/jsp_jstl.jsp" %>
<%@ include file="../include/jsp_page_id.jsp" %>
<%@ include file="../include/jsp_method_get_string_value.jsp" %>
<%@ include file="../include/html_doctype.jsp" %>
<%
//标题标签I18N关键字，不允许null
String titleMessageKey = getStringValue(request, SchemaController.KEY_TITLE_MESSAGE_KEY);
//表单提交action，允许为null
String formAction = getStringValue(request, SchemaController.KEY_FORM_ACTION, "#");
//是否只读操作，允许为null
boolean readonly = ("true".equalsIgnoreCase(getStringValue(request, SchemaController.KEY_READONLY)));
%>
<html>
<head>
<%@ include file="../include/html_head.jsp" %>
<title><%@ include file="../include/html_title_app_name.jsp" %><fmt:message key='<%=titleMessageKey%>' /></title>
</head>
<body>
<div id="${pageId}" class="schema-form">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/schema/<%=formAction%>" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<input type="hidden" name="id" value="<c:out value='${schema.id}' />">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.title' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="title" value="<c:out value='${schema.title}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.url' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="url" value="<c:out value='${schema.url}' />" style="width:35em;" class="ui-widget ui-widget-content" />
					<%if(!readonly){%>
					<span id="schemaBuildUrlHelp" class="ui-state-default ui-corner-all" style="cursor: pointer;" title="<fmt:message key='schema.urlHelp' />"><span class="ui-icon ui-icon-help"></span></span>&nbsp;
					<%}%>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.user' /></label>
				</div>
				<div class="form-item-value">
					<input type="text" name="user" value="<c:out value='${schema.user}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%if(!readonly){%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.password' /></label>
				</div>
				<div class="form-item-value">
					<input type="password" name="password" value="<c:out value='${schema.password}' />" class="ui-widget ui-widget-content" />
				</div>
			</div>
			<%}%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schema.shared' /></label>
				</div>
				<div class="form-item-value">
					<div class="schema-shared-radios">
					<label for="${pageId}-schemaSharedYes"><fmt:message key='yes' /></label>
		   			<input type="radio" id="${pageId}-schemaSharedYes" name="shared" value="1" <c:if test='${schema.shared}'>checked="checked"</c:if> />
					<label for="${pageId}-schemaSharedNo"><fmt:message key='no' /></label>
		   			<input type="radio" id="${pageId}-schemaSharedNo" name="shared" value="0" <c:if test='${!schema.shared}'>checked="checked"</c:if> />
		   			</div>
				</div>
			</div>
			<%if(!readonly){%>
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='advancedSetting' /></label>
				</div>
				<div class="form-item-value">
					<button id="schemaAdvancedSet" type="button" style="font-size: 0.8em;">&nbsp;</button>
				</div>
			</div>
			<%}%>
			<div class="form-item" id="schemaDriverEntityFormItem">
				<div class="form-item-label">
					<label><fmt:message key='schema.driverEntity' /></label>
				</div>
				<div id="driverEntityFormItemValue" class="form-item-value">
					<input type="hidden" id="driverEntityId" name="driverEntity.id" value="<c:out value='${schema.driverEntity.id}' />" />
					<input type="text" id="driverEntityText" value="<c:out value='${schema.driverEntity.displayText}' />" size="20" readonly="readonly" class="ui-widget ui-widget-content" />
					<%if(!readonly){%>
					<div id="driverEntityActionGroup">
						<button id="driverEntitySelectButton" type="button"><fmt:message key='select' /></button>
						<select id="driverEntityActionSelect">
							<option value='del'><fmt:message key='delete' /></option>
						</select>
					</div>
					<%}%>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<%if(!readonly){%>
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
			<%}%>
		</div>
	</form>
</div>
<%@ include file="../include/page_js_obj.jsp" %>
<script type="text/javascript">
(function(pageObj)
{
	pageObj.form = pageObj.element("#${pageId}-form");
	pageObj.driverEntityFormItemValue =  pageObj.element("#driverEntityFormItemValue");
	pageObj.schemaDriverEntityFormItem = pageObj.element("#schemaDriverEntityFormItem");
	pageObj.isDriverEntityEmpty = (pageObj.element("input[name='driverEntity.id']").val() == "");
	
	<%if(!readonly){%>
	
	pageObj.element("#schemaBuildUrlHelp").click(function()
	{
		pageObj.open(contextPath+"/schemaUrlBuilder/buildUrl",
		{
			width: "60%",
			pageParam :
			{
				"initUrl" : pageObj.element("input[name='url']").val(),
				"setSchemaUrl" : function(url)
				{
					pageObj.element("input[name='url']").val(url);
				}
			}
		});
	});
	
	pageObj.element("#driverEntitySelectButton").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(driverEntity)
				{
					pageObj.element("input[name='driverEntity.id']").val(driverEntity.id);
					pageObj.element("#driverEntityText").val(driverEntity.displayText);
				}
			}
		};
		$.setGridPageHeightOption(options);
		pageObj.open(contextPath+"/driverEntity/select", options);
	});
	
	pageObj.element("#driverEntityActionSelect").selectmenu(
	{
		appendTo: pageObj.driverEntityFormItemValue,
		classes:
		{
	          "ui-selectmenu-button": "ui-button-icon-only splitbutton-select"
	    },
	    select: function(event, ui)
    	{
    		var action = $(ui.item).attr("value");
    		
			if("del" == action)
    		{
				pageObj.element("#driverEntityId").val("");
				pageObj.element("#driverEntityText").val("");
    		}
    	}
	});
	
	pageObj.element("#driverEntityActionGroup").controlgroup();
	
	pageObj.form.validate(
	{
		rules :
		{
			title : "required",
			url : "required"
		},
		messages :
		{
			title : "<fmt:message key='validation.required' />",
			url : "<fmt:message key='validation.required' />"
		},
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function()
				{
					var pageParam = pageObj.pageParam();
					
					var close = true;
					
					if(pageParam && pageParam.afterSave)
						close = (pageParam.afterSave() != false);
					
					if(close)
						pageObj.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item"));
		}
	});
	<%}%>
	
	pageObj.element("input[name='shared']").checkboxradio({icon:false});
	pageObj.element(".schema-shared-radios").controlgroup();
	
	$("input:submit, input:button, input:reset, button", pageObj.page).button();
	
	if(pageObj.isDriverEntityEmpty)
		pageObj.schemaDriverEntityFormItem.hide();
	
	<%if(!readonly){%>
	$("#schemaAdvancedSet", pageObj.page).button(
	{
		icon: (pageObj.schemaDriverEntityFormItem.is(":hidden") ? "ui-icon-triangle-1-s" : "ui-icon-triangle-1-n"),
		showLabel: false
	})
	.click(function()
	{
		if(pageObj.schemaDriverEntityFormItem.is(":hidden"))
		{
			pageObj.schemaDriverEntityFormItem.show();
			$(this).button("option", "icon", "ui-icon-triangle-1-n");
		}
		else
		{
			pageObj.schemaDriverEntityFormItem.hide();
			$(this).button("option", "icon", "ui-icon-triangle-1-s");
		}
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>