/*
 * Copyright 2018-2023 datagear.tech
 *
 * This file is part of DataGear.
 *
 * DataGear is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * DataGear is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with DataGear.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.datagear.web.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.datagear.analysis.Chart;
import org.datagear.analysis.support.ChartWidget;
import org.datagear.analysis.support.html.HtmlTplDashboard;
import org.datagear.analysis.support.html.LoadableChartWidgets;
import org.datagear.web.util.ExpiredSessionAttrManager.ExpiredSessionAttr;

/**
 * 会话中的看板信息支持类。
 * 
 * @author datagear@163.com
 *
 */
public class SessionDashboardInfoSupport
{
	/** 看板心跳频率：5分钟 */
	public static final long DASHBOARD_HEARTBEAT_INTERVAL_MS = 1000 * 60 * 5;

	private ExpiredSessionAttrManager expiredSessionAttrManager = new ExpiredSessionAttrManager();

	public SessionDashboardInfoSupport()
	{
		super();
	}

	public ExpiredSessionAttrManager getExpiredSessionAttrManager()
	{
		return expiredSessionAttrManager;
	}

	public void setExpiredSessionAttrManager(ExpiredSessionAttrManager expiredSessionAttrManager)
	{
		this.expiredSessionAttrManager = expiredSessionAttrManager;
	}

	/**
	 * 在会话中存储{@linkplain DashboardInfo}。
	 * 
	 * @param request
	 * @param di
	 */
	public void setDashboardInfo(HttpServletRequest request, DashboardInfo di)
	{
		String name = toSessionNameForDashboardInfo(di.getDashboardId());
		getExpiredSessionAttrManager().setAttr(request, name, di);
	}

	/**
	 * 获取会话中存储的{@linkplain DashboardInfo}。
	 * 
	 * @param request
	 * @param dashboardId
	 * @return 返回{@code null}表示没有
	 */
	public DashboardInfo getDashboardInfo(HttpServletRequest request, String dashboardId)
	{
		String name = toSessionNameForDashboardInfo(dashboardId);
		return getExpiredSessionAttrManager().getAttr(request, name);
	}

	/**
	 * 为会话中的{@linkplain DashboardInfo}添加图表信息。
	 * 
	 * @param request
	 * @param di
	 * @param chartIdToChartWidgetIds
	 */
	public void addDashboardInfoCharts(HttpServletRequest request, DashboardInfo di,
			Map<String, String> chartIdToChartWidgetIds)
	{
		di.putChartWidgetIds(chartIdToChartWidgetIds);

		// 无论di是否已存储至会话，这里都应再执行存储，以为可能扩展的分布式会话提供支持
		String name = toSessionNameForDashboardInfo(di.getDashboardId());
		getExpiredSessionAttrManager().setAttr(request, name, di);
	}

	/**
	 * 更新会话中的{@linkplain DashboardInfo}的访问时间。
	 * 
	 * @param request
	 * @param dashboardId
	 * @param time
	 */
	public void updateDashboardInfoAccess(HttpServletRequest request, String dashboardId, long time)
	{
		String name = toSessionNameForDashboardInfo(dashboardId);
		getExpiredSessionAttrManager().updateAccessTime(request, name, time);
	}

	/**
	 * 移除会话中过期的看板信息。
	 * <p>
	 * 需定时清理，防止会话中存储过多已过期的看板信息
	 * </p>
	 * 
	 * @param request
	 */
	public void removeDashboardInfoExpired(HttpServletRequest request, int expiredMs)
	{
		getExpiredSessionAttrManager().removeExpired(request, expiredMs, DashboardInfo.class);
	}

	/**
	 * 移除会话中过期的看板信息。
	 * <p>
	 * 需定时清理，防止会话中存储过多已过期的看板信息
	 * </p>
	 * 
	 * @param request
	 */
	public void removeDashboardInfoExpired(HttpServletRequest request)
	{
		getExpiredSessionAttrManager().removeExpired(request, DASHBOARD_HEARTBEAT_INTERVAL_MS * 3, DashboardInfo.class);
	}

	public String toSessionNameForDashboardInfo(String dashboardId)
	{
		return DashboardInfo.class.getSimpleName() + "-" + dashboardId;
	}

	/**
	 * 看板信息。
	 * 
	 * @author datagear@163.com
	 *
	 */
	public static class DashboardInfo implements ExpiredSessionAttr
	{
		private static final long serialVersionUID = 1L;

		/** 看板ID */
		private final String dashboardId;

		/** 看板部件ID */
		private final String dashboardWidgetId;

		private final LoadableChartWidgets loadableChartWidgets;

		/** 图表ID-图表部件ID映射表 */
		private final Map<String, String> chartIdToChartWidgetIds = new HashMap<String, String>();

		private final boolean showForEdit;

		private volatile long lastAccessTime = 0;

		public DashboardInfo(HtmlTplDashboard dashboard, boolean showForEdit)
		{
			this.dashboardId = dashboard.getId();
			this.dashboardWidgetId = dashboard.getWidget().getId();
			this.loadableChartWidgets = dashboard.getLoadableChartWidgets();

			if (dashboard.hasChart())
			{
				List<Chart> charts = dashboard.getCharts();
				for (Chart chart : charts)
					this.chartIdToChartWidgetIds.put(chart.getId(), ChartWidget.getChartWidgetId(chart));
			}

			this.showForEdit = showForEdit;
			this.lastAccessTime = System.currentTimeMillis();
		}

		public String getDashboardId()
		{
			return dashboardId;
		}

		public String getDashboardWidgetId()
		{
			return dashboardWidgetId;
		}

		/**
		 * @return 可能为{@code null}
		 */
		public LoadableChartWidgets getLoadableChartWidgets()
		{
			return loadableChartWidgets;
		}

		public synchronized Map<String, String> getChartIdToChartWidgetIds()
		{
			return Collections.unmodifiableMap(chartIdToChartWidgetIds);
		}

		public boolean isShowForEdit()
		{
			return showForEdit;
		}

		public synchronized String getChartWidgetId(String chartId)
		{
			return this.chartIdToChartWidgetIds.get(chartId);
		}

		public synchronized void putChartWidgetId(String chartId, String chartWidgetId)
		{
			this.chartIdToChartWidgetIds.put(chartId, chartWidgetId);
		}

		public synchronized void putChartWidgetIds(Map<String, String> chartIdToChartWidgetIds)
		{
			this.chartIdToChartWidgetIds.putAll(chartIdToChartWidgetIds);
		}

		@Override
		public long getLastAccessTime()
		{
			return this.lastAccessTime;
		}

		@Override
		public void setLastAccessTime(long time)
		{
			this.lastAccessTime = time;
		}
	}
}
