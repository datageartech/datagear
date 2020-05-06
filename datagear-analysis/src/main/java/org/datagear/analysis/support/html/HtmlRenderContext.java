/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis.support.html;

import java.io.Writer;

import org.datagear.analysis.RenderContext;

/**
 * HTML渲染上下文。
 * 
 * @author datagear@163.com
 *
 */
public interface HtmlRenderContext extends RenderContext
{
	/**
	 * 获取{@linkplain WebContext}。
	 * 
	 * @return
	 */
	WebContext getWebContext();

	/**
	 * 获取渲染输出流。
	 * 
	 * @return
	 */
	Writer getWriter();

	/**
	 * 生成下一个序号。
	 * <p>
	 * 此方法为生成HTML页面元素ID、变量名提供支持。
	 * </p>
	 * 
	 * @return
	 */
	int nextSequence();

	/**
	 * Web上下文信息。
	 * <p>
	 * 这些信息可以输出值客户端，提供看板交互支持。
	 * </p>
	 * 
	 * @author datagear@163.com
	 *
	 */
	class WebContext
	{
		/** 上下文路径 */
		private String contextPath;

		/** 更新看板数据的URL */
		private String updateDashboardURL;

		/** 更新看板数据的的看板ID参数名 */
		private String dashboardIdParam = "dashboardId";

		/** 更新看板数据的图表集参数名 */
		private String chartIdsParam = "chartsId";

		/** 更新看板数据的图表集参数值的参数名 */
		private String chartsParamValuesParam = "chartsParamValues";

		public WebContext()
		{
			super();
		}

		public WebContext(String contextPath, String updateDashboardURL)
		{
			super();
			this.contextPath = contextPath;
			this.updateDashboardURL = updateDashboardURL;
		}

		public String getContextPath()
		{
			return contextPath;
		}

		public void setContextPath(String contextPath)
		{
			this.contextPath = contextPath;
		}

		public String getUpdateDashboardURL()
		{
			return updateDashboardURL;
		}

		public void setUpdateDashboardURL(String updateDashboardURL)
		{
			this.updateDashboardURL = updateDashboardURL;
		}

		public String getDashboardIdParam()
		{
			return dashboardIdParam;
		}

		public void setDashboardIdParam(String dashboardIdParam)
		{
			this.dashboardIdParam = dashboardIdParam;
		}

		public String getChartIdsParam()
		{
			return chartIdsParam;
		}

		public void setChartIdsParam(String chartIdsParam)
		{
			this.chartIdsParam = chartIdsParam;
		}

		public String getChartsParamValuesParam()
		{
			return chartsParamValuesParam;
		}

		public void setChartsParamValuesParam(String chartsParamValuesParam)
		{
			this.chartsParamValuesParam = chartsParamValuesParam;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + " [contextPath=" + contextPath + ", updateDashboardURL="
					+ updateDashboardURL + ", dashboardIdParam=" + dashboardIdParam + ", chartIdsParam=" + chartIdsParam
					+ ", chartsParamValuesParam=" + chartsParamValuesParam + "]";
		}
	}
}
