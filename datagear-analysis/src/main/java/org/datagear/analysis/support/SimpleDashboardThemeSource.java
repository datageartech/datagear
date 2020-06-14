/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.ArrayList;
import java.util.List;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.Theme;

/**
 * 简单{@linkplain DashboardThemeSource}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDashboardThemeSource implements DashboardThemeSource
{
	public static final DashboardTheme THEME_LIGHT = new DashboardTheme("light", "#333", "#FFF", "#E4E4E4",
			new ChartTheme("lightChart", "#333", "transparent", "#FFF", "#E4E4E4", "#333", "#666",
					new String[] { "#2EC7C9", "#B6A2DE", "#FFB980", "#97B552", "#D87A80", "#8D98B3", "#E5CF0D",
							"#5AB1EF", "#95706D", "#DC69AA" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" },
					new Theme("lightChartTooltip", "#333", "#DDD", "#BBB"),
					new Theme("lightChartHighlight", "#FFF", "#007FFF", "#CCC")));

	public static final DashboardTheme THEME_DARK = new DashboardTheme("dark", "#EEE", "#000", "#555",
			new ChartTheme("darkChart", "#EEE", "transparent", "#000", "#555", "#EEE", "#BBB",
					new String[] { "#00CDCD", "#91CA8C", "#EA7E53", "#24666C", "#73A373", "#019DA2", "#EEDD78",
							"#73B9BC", "#7289AB", "#12CDD2" },
					new String[] { "#58A52D", "#FFD700", "#FF4500" },
					new Theme("darkChartTooltip", "#EEE", "#444", "#555"),
					new Theme("darkChartHighlight", "#FFF", "#1E90FF", "#555")));

	private List<DashboardTheme> dashboardThemes;

	public SimpleDashboardThemeSource()
	{
		super();
		this.dashboardThemes = new ArrayList<>();
		this.dashboardThemes.add(THEME_LIGHT);
		this.dashboardThemes.add(THEME_DARK);
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
