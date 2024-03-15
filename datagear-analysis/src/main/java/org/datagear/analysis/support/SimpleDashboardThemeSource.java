/*
 * Copyright 2018-present datagear.tech
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

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.NameAwareUtil;

/**
 * 简单{@linkplain DashboardThemeSource}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDashboardThemeSource implements DashboardThemeSource
{
	// 2.5.0版本默认主题（ECharts-4.9.0）
	// public static final DashboardTheme THEME_LIGHT = new
	// DashboardTheme("light", "#333", "#FFF",
	// new ChartTheme("lightChart", "#333", "transparent", "#FFF",
//					new String[] { "#2EC7C9", "#B6A2DE", "#FFB980", "#97B552", "#D87A80", "#8D98B3", "#E5CF0D",
//							"#5AB1EF", "#95706D", "#DC69AA" },
	// new String[] { "#58A52D", "#FFD700", "#FF4500" }));
//
	// public static final DashboardTheme THEME_DARK = new
	// DashboardTheme("dark", "#EEE", "#000",
	// new ChartTheme("darkChart", "#EEE", "transparent", "#000",
//					new String[] { "#00CDCD", "#91CA8C", "#EA7E53", "#24666C", "#73A373", "#019DA2", "#EEDD78",
//							"#73B9BC", "#7289AB", "#12CDD2" },
	// new String[] { "#58A52D", "#FFD700", "#FF4500" }));
//
	// public static final DashboardTheme THEME_GREEN = new
	// DashboardTheme("green", "#FFFFFF", "#285C00",
	// new ChartTheme("greenChart", "#FFFFFF", "transparent", "#285C00",
//					new String[] { "#2EC7C9", "#B6A2DE", "#FFB980", "#97B552", "#D87A80", "#8D98B3", "#E5CF0D",
//							"#5AB1EF", "#95706D", "#DC69AA" },
	// new String[] { "#58A52D", "#FFD700", "#FF4500" }));

// 2.6.0版本主题（ECharts-5.1.2）
	public static final DashboardTheme THEME_LIGHT = new DashboardTheme("light", "#000", "#FFF",
			new ChartTheme("lightChart", "#000", "transparent", "#FFF",
					new String[] { "#5470C6", "#91CC75", "#FAC858", "#EE6666", "#73C0DE", "#3BA272", "#FC8452",
							"#9A60B4", "#EA7CCC", "#B6A2DE" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" }));

	public static final DashboardTheme THEME_DARK = new DashboardTheme("dark", "#EEE", "#17212F",
			new ChartTheme("darkChart", "#EEE", "transparent", "#17212F",
					new String[] { "#73C0DE", "#91CC75", "#FAC858", "#EE6666", "#B6A2DE", "#3BA272", "#FC8452",
							"#9A60B4", "#EA7CCC", "#5470C6" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" }));

	public static final DashboardTheme THEME_GREEN = new DashboardTheme("green", "#FFFFFF", "#285C00",
			new ChartTheme("greenChart", "#FFFFFF", "transparent", "#285C00",
					new String[] { "#2EC7C9", "#B6A2DE", "#FFB980", "#97B552", "#D87A80", "#8D98B3", "#E5CF0D",
							"#5AB1EF", "#95706D", "#DC69AA" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" }));

	static
	{
		THEME_LIGHT.getChartTheme().setGradient(20);
		THEME_DARK.getChartTheme().setGradient(10);
		THEME_GREEN.getChartTheme().setGradient(10);
	}

	private List<DashboardTheme> dashboardThemes;

	public SimpleDashboardThemeSource()
	{
		super();
		this.dashboardThemes = new ArrayList<>();
		this.dashboardThemes.add(THEME_LIGHT);
		this.dashboardThemes.add(THEME_DARK);
		this.dashboardThemes.add(THEME_GREEN);
	}

	public SimpleDashboardThemeSource(List<DashboardTheme> dashboardThemes)
	{
		super();
		this.dashboardThemes = dashboardThemes;
	}

	public List<DashboardTheme> getDashboardThemes()
	{
		return dashboardThemes;
	}

	public void setDashboardThemes(List<DashboardTheme> dashboardThemes)
	{
		this.dashboardThemes = dashboardThemes;
	}

	@Override
	public DashboardTheme getDashboardTheme()
	{
		return THEME_LIGHT;
	}

	@Override
	public DashboardTheme getDashboardTheme(String themeName)
	{
		return NameAwareUtil.find(this.dashboardThemes, themeName);
	}
}
