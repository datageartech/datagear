<%--
/*
 * Copyright (c) 2018 by datagear.org.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@page import="com.alibaba.fastjson.serializer.SerializeConfig"%>
<%@page import="com.alibaba.fastjson.serializer.JSONSerializer"%>
<%@page import="com.alibaba.fastjson.serializer.SerializeWriter"%>
<%@ page import="com.alibaba.fastjson.serializer.SerializerFeature" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ page import="org.springframework.web.context.WebApplicationContext"%>
<%@ page import="java.io.Writer"%>
<%@ page import="java.io.IOException"%>
<%!
protected void writeJson(ServletContext servletContext, Writer out, Object object) throws IOException
{
	WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

	SerializeConfig fastJsonConfig = (SerializeConfig)webApplicationContext.getBean("serializeConfig");
	SerializerFeature[] serializerFeatures = (SerializerFeature[])webApplicationContext.getBean("serializerFeatures");
	
	SerializeWriter serializeWriter = new SerializeWriter(out, serializerFeatures);
	JSONSerializer serializer = new JSONSerializer(serializeWriter, fastJsonConfig);
	
	try
	{
		serializer.write(object);
	}
	finally
	{
		serializer.close();
	}
}
%>
