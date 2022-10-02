/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support.html;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 看板可在页面通过JS异步加载的图表部件模式。
 * 
 * @author datagear@163.com
 * 
 */
public class LoadableChartWidgetsPattern implements Serializable
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

	public LoadableChartWidgetsPattern(String pattern)
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
	
	public static LoadableChartWidgetsPattern all()
	{
		return new LoadableChartWidgetsPattern(PATTERN_ALL);
	}
	
	public static LoadableChartWidgetsPattern none()
	{
		return new LoadableChartWidgetsPattern(PATTERN_NONE);
	}
	
	public static LoadableChartWidgetsPattern permitted()
	{
		return new LoadableChartWidgetsPattern(PATTERN_PERMITTED);
	}
	
	public static LoadableChartWidgetsPattern list(Set<String> chartWidgetIds)
	{
		LoadableChartWidgetsPattern re = new LoadableChartWidgetsPattern(PATTERN_LIST);
		re.setChartWidgetIds(chartWidgetIds);
		
		return re;
	}
}
