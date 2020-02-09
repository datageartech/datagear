/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.HashMap;
import java.util.Map;

import org.datagear.analysis.ChartTheme;
import org.datagear.analysis.DashboardTheme;
import org.datagear.analysis.DashboardThemeSource;
import org.datagear.analysis.RenderStyle;
import org.datagear.analysis.Theme;

/**
 * 简单{@linkplain DashboardThemeSource}。
 * 
 * @author datagear@163.com
 *
 */
public class SimpleDashboardThemeSource implements DashboardThemeSource
{
	public static final DashboardTheme THEME_LIGHT = new DashboardTheme("#FFF", "#333", "#c5c5c5",
			new ChartTheme("#FBFBFB", "#333", "#c5c5c5", "#333", "#AAA", "#DDD", "#007FFF", new String[] { "#2EC7C9",
					"#B6A2DE", "#5AB1EF", "#FFB980", "#D87A80", "#8D98B3", "#E5CF0D", "#97B552", "#95706D", "#DC69AA" },
					new Theme("#FFF", "#333", "#c5c5c5")));

	public static final DashboardTheme THEME_DARK = new DashboardTheme("#000", "#EEE", "#333",
			new ChartTheme("#101010", "#EEE", "#202020", "#AAA", "#888", "#444", "#F58400", new String[] { "#5EF6FE",
					"#91CA8C", "#EA7E53", "#24666C", "#73A373", "#019DA2", "#EEDD78", "#73B9BC", "#7289AB", "#12CDD2" },
					new Theme("#000", "#EEE", "#333")));

	private Map<RenderStyle, DashboardTheme> dashboardThemes;

	public SimpleDashboardThemeSource()
	{
		super();
		this.dashboardThemes = getDefaultDashboardThemes();
	}

	public SimpleDashboardThemeSource(Map<RenderStyle, DashboardTheme> dashboardThemes)
	{
		super();
		this.dashboardThemes = dashboardThemes;
	}

	public Map<RenderStyle, DashboardTheme> getDashboardThemes()
	{
		return dashboardThemes;
	}

	public void setDashboardThemes(Map<RenderStyle, DashboardTheme> dashboardThemes)
	{
		this.dashboardThemes = dashboardThemes;
	}

	@Override
	public DashboardTheme getDashboardTheme()
	{
		return THEME_LIGHT;
	}

	@Override
	public DashboardTheme getDashboardTheme(RenderStyle renderStyle)
	{
		return (this.dashboardThemes == null ? null : this.dashboardThemes.get(renderStyle));
	}

	public static Map<RenderStyle, DashboardTheme> getDefaultDashboardThemes()
	{
		Map<RenderStyle, DashboardTheme> dashboardThemes = new HashMap<RenderStyle, DashboardTheme>();

		dashboardThemes.put(RenderStyle.LIGHT, THEME_LIGHT);
		dashboardThemes.put(RenderStyle.DARK, THEME_DARK);

		return dashboardThemes;
	}
}
