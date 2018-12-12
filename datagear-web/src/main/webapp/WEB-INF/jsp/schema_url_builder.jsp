<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="include/jsp_import.jsp" %>
<%@ include file="include/jsp_ajax_request.jsp" %>
<%@ include file="include/jsp_jstl.jsp" %>
<%@ include file="include/jsp_page_id.jsp" %>
<%@ include file="include/jsp_method_get_string_value.jsp" %>
<%@ include file="include/html_doctype.jsp" %>
<html>
<head>
<%@ include file="include/html_head.jsp" %>
<title><%@ include file="include/html_title_app_name.jsp" %><fmt:message key='schemaUrlBuilder.schemaUrlBuilder' /></title>
</head>
<body>
<div id="${pageId}" class="page-form page-form-schemaUrlBuilder">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/schemaUrlBuilder/saveScriptCode" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schemaUrlBuilder.scriptCode' /></label>
				</div>
				<div class="form-item-value form-item-value-scriptCode">
					<textarea name="scriptCode" class="ui-widget ui-widget-content script-code-textarea"><c:out value='${scriptCode}' /></textarea>
					<div class="script-code-note">
					<span><fmt:message key='schemaUrlBuilder.scriptCodeNote.0' /></span>
					<pre>
{
   //<fmt:message key='schemaUrlBuilder.scriptCodeNote.required' /><fmt:message key='comma' /><fmt:message key='schemaUrlBuilder.scriptCodeNote.dbType' />
   dbType : "...",
   
   //<fmt:message key='schemaUrlBuilder.scriptCodeNote.required' /><fmt:message key='comma' /><fmt:message key='schemaUrlBuilder.scriptCodeNote.template' />
   template : "...{host}...{port}...{name}...",
   
   //<fmt:message key='schemaUrlBuilder.scriptCodeNote.optional' /><fmt:message key='comma' /><fmt:message key='schemaUrlBuilder.scriptCodeNote.defaultValue' />
   defaultValue : { host : "...", port : "...", name : "" },
   
   //<fmt:message key='schemaUrlBuilder.scriptCodeNote.optional' /><fmt:message key='comma' /><fmt:message key='schemaUrlBuilder.scriptCodeNote.order' />
   order : 6
}</pre>
					<span><fmt:message key='schemaUrlBuilder.scriptCodeNote.1' /></span>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<button id="previewScriptCode" type="button" class="preview-script-code-button"><fmt:message key='preview' /></button>
				</div>
			</div>
		</div>
		<div class="form-foot" style="text-align:center;">
			<input type="submit" value="<fmt:message key='save' />" class="recommended" />
			&nbsp;&nbsp;
			<input type="reset" value="<fmt:message key='reset' />" />
		</div>
	</form>
</div>
<%@ include file="include/page_js_obj.jsp" %>
<%@ include file="include/page_obj_form.jsp" %>
<script type="text/javascript">
(function(po)
{
	$.initButtons(po.element());
	
	po.element("#previewScriptCode").click(function()
	{
		po.open(contextPath+"/schemaUrlBuilder/previewScriptCode",
		{
			data : { "scriptCode" : po.element("textarea[name='scriptCode']").val() }
		});
	});
	
	po.form().validate(
	{
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var pageParam = po.pageParam();
					
					var close = false;
					
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
})
(${pageId});
</script>
</body>
</html>