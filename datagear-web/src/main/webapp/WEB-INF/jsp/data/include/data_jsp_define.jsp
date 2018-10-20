<%--
/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */
--%>
<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="java.util.List"%>
<%@ page import="org.datagear.model.Model"%>
<%@ page import="org.datagear.model.Property"%>
<%@ page import="org.datagear.model.support.DynamicBean"%>
<%@ page import="org.datagear.model.support.MU"%>
<%@ page import="org.datagear.model.support.PropertyPath"%>
<%@ page import="org.datagear.model.support.PropertyPathInfo"%>
<%@ page import="org.datagear.management.domain.Schema"%>
<%@ page import="org.datagear.persistence.PagingData"%>
<%@ page import="org.datagear.web.util.ModelUtils" %>
<%
Schema schema = (Schema)request.getAttribute("schema");
Model model = (Model)request.getAttribute("model");
%>