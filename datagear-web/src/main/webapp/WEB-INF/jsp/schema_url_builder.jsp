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
<div id="${pageId}" class="page-data-form page-data-form-schemaUrlBuilder">
	<form id="${pageId}-form" action="<%=request.getContextPath()%>/schemaUrlBuilder/saveScriptCode" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><fmt:message key='schemaUrlBuilder.scriptCode' /></label>
				</div>
				<div class="form-item-value">
					<textarea name="scriptCode" style="width:30em; height: 20em;"><c:out value='${scriptCode}' /></textarea>
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
<script type="text/javascript">
(function(pageObj)
{
	$.initButtons(pageObj.element());
	
	pageObj.form = pageObj.element("#${pageId}-form");
	
	pageObj.form.validate(
	{
		submitHandler : function(form)
		{
			$(form).ajaxSubmit(
			{
				success : function(response)
				{
					var pageParam = pageObj.pageParam();
					
					var close = false;
					
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
})
(${pageId});
</script>
</body>
</html>