/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.analysis;

import java.util.Map;

/**
 * 图表属性值集合。
 * <p>
 * 此类用于表示用户对{@linkplain ChartPlugin#getChartProperties()}的输入集。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ChartPropertyValues extends AbstractDelegatedMap<String, Object>
{
	public ChartPropertyValues()
	{
		super();
	}

	public ChartPropertyValues(Map<String, Object> chartPropertyValues)
	{
		super(chartPropertyValues);
	}
}
