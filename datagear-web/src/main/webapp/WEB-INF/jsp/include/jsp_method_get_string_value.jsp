<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%!
protected String getStringValue(HttpServletRequest request, String name)
{
	Object attrValue = request.getAttribute(name);
	
	if(attrValue != null && attrValue instanceof String)
		return (String)attrValue;
	
	return request.getParameter(name);
}

protected String getStringValue(HttpServletRequest request, String name, String defaultValue)
{
	String value = getStringValue(request, name);
	
	return (value == null ? "" : value);
}
%>