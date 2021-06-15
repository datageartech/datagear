/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;

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
	public static final DashboardTheme THEME_LIGHT = new DashboardTheme("light", "#333", "#FFF",
			new ChartTheme("lightChart", "#333", "transparent", "#FFF",
					new String[] { "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de", "#3ba272", "#fc8452",
							"#9a60b4", "#ea7ccc", "#B6A2DE" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" }));

	public static final DashboardTheme THEME_DARK = new DashboardTheme("dark", "#EEE", "#000",
			new ChartTheme("darkChart", "#EEE", "transparent", "#000",
					new String[] { "#73c0de", "#91cc75", "#fac858", "#ee6666", "#B6A2DE", "#3ba272", "#fc8452",
							"#9a60b4", "#ea7ccc", "#5470c6" },
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
		for (DashboardTheme theme : this.dashboardThemes)
		{
			if (theme.getName().equals(themeName))
				return theme;
		}

		return null;
	}
}
