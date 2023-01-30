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

package org.datagear.analysis.support.html;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 看板可在页面通过JS异步加载的图表部件。
 * 
 * @author datagear@163.com
 * 
 */
public class LoadableChartWidgets implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 全部 */
	public static final String PATTERN_ALL = "all";
	
	/** 无 */
	public static final String PATTERN_NONE = "none";
	
	/** 授权批准的 */
	public static final String PATTERN_PERMITTED = "permitted";
	
	/** 指定清单内的 */
	public static final String PATTERN_LIST = "list";
	
	private String pattern = PATTERN_ALL;
	
	private Set<String> chartWidgetIds = Collections.emptySet();

	public LoadableChartWidgets(String pattern)
	{
		super();
		this.pattern = pattern;
	}

	public String getPattern()
	{
		return pattern;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public Set<String> getChartWidgetIds()
	{
		return chartWidgetIds;
	}

	public void setChartWidgetIds(Set<String> chartWidgetIds)
	{
		this.chartWidgetIds = chartWidgetIds;
	}
	
	public boolean isPatternAll()
	{
		return PATTERN_ALL.equals(this.pattern);
	}
	
	public boolean isPatternNone()
	{
		return PATTERN_NONE.equals(this.pattern);
	}
	
	public boolean isPatternPermitted()
	{
		return PATTERN_PERMITTED.equals(this.pattern);
	}
	
	public boolean isPatternList()
	{
		return PATTERN_LIST.equals(this.pattern);
	}
	
	public boolean inList(String chartWidgetId)
	{
		if(this.chartWidgetIds == null)
			return false;
		
		return this.chartWidgetIds.contains(chartWidgetId);
	}
	
	public static LoadableChartWidgets all()
	{
		return new LoadableChartWidgets(PATTERN_ALL);
	}
	
	public static LoadableChartWidgets none()
	{
		return new LoadableChartWidgets(PATTERN_NONE);
	}
	
	public static LoadableChartWidgets permitted()
	{
		return new LoadableChartWidgets(PATTERN_PERMITTED);
	}
	
	public static LoadableChartWidgets list(Set<String> chartWidgetIds)
	{
		LoadableChartWidgets re = new LoadableChartWidgets(PATTERN_LIST);
		re.setChartWidgetIds(chartWidgetIds);
		
		return re;
	}
}
