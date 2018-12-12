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
					<input type="text" name="url" value="<c:out value='${schema.url}' />" class="ui-widget ui-widget-content" />
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
<%@ include file="../include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	po.driverEntityFormItemValue = function(){ return this.element("#driverEntityFormItemValue"); };
	po.schemaDriverEntityFormItem = function(){ return this.element("#schemaDriverEntityFormItem"); };
	po.isDriverEntityEmpty = (po.element("input[name='driverEntity.id']").val() == "");
	
	<%if(!readonly){%>
	
	po.element("#schemaBuildUrlHelp").click(function()
	{
		po.open(contextPath+"/schemaUrlBuilder/buildUrl",
		{
			data : { url : po.element("input[name='url']").val() },
			width: "60%",
			pageParam :
			{
				"setSchemaUrl" : function(url)
				{
					po.element("input[name='url']").val(url);
				}
			}
		});
	});
	
	po.element("#driverEntitySelectButton").click(function()
	{
		var options =
		{
			pageParam :
			{
				submit : function(driverEntity)
				{
					po.element("input[name='driverEntity.id']").val(driverEntity.id);
					po.element("#driverEntityText").val(driverEntity.displayText);
				}
			}
		};
		$.setGridPageHeightOption(options);
		po.open(contextPath+"/driverEntity/select", options);
	});
	
	po.element("#driverEntityActionSelect").selectmenu(
	{
		appendTo: po.driverEntityFormItemValue(),
		classes:
		{
	          "ui-selectmenu-button": "ui-button-icon-only splitbutton-select"
	    },
	    select: function(event, ui)
    	{
    		var action = $(ui.item).attr("value");
    		
			if("del" == action)
    		{
				po.element("#driverEntityId").val("");
				po.element("#driverEntityText").val("");
    		}
    	}
	});
	
	po.element("#driverEntityActionGroup").controlgroup();
	
	po.form().validate(
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
					var pageParam = po.pageParam();
					
					var close = true;
					
					if(pageParam && pageParam.afterSave)
						close = (pageParam.afterSave() != false);
					
					if(close)
						po.close();
				}
			});
		},
		errorPlacement : function(error, element)
		{
			error.appendTo(element.closest(".form-item-value"));
		}
	});
	<%}%>
	
	po.element("input[name='shared']").checkboxradio({icon:false});
	po.element(".schema-shared-radios").controlgroup();
	
	$.initButtons(po.element());
	
	if(po.isDriverEntityEmpty)
		po.schemaDriverEntityFormItem().hide();
	
	<%if(!readonly){%>
	$("#schemaAdvancedSet", po.page).button(
	{
		icon: (po.schemaDriverEntityFormItem().is(":hidden") ? "ui-icon-triangle-1-s" : "ui-icon-triangle-1-n"),
		showLabel: false
	})
	.click(function()
	{
		var item = po.schemaDriverEntityFormItem();
		
		if(item.is(":hidden"))
		{
			item.show();
			$(this).button("option", "icon", "ui-icon-triangle-1-n");
		}
		else
		{
			item.hide();
			$(this).button("option", "icon", "ui-icon-triangle-1-s");
		}
	});
	<%}%>
})
(${pageId});
</script>
</body>
</html>