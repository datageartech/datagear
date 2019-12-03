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
public class ChartPropertyValues
{
	private Map<String, ?> options;

	public ChartPropertyValues()
	{
	}

	public ChartPropertyValues(Map<String, ?> options)
	{
		super();
		this.options = options;
	}

	public boolean hasOption()
	{
		return (this.options != null && !this.options.isEmpty());
	}

	public Map<String, ?> getOptions()
	{
		return options;
	}

	public void setOptions(Map<String, ?> options)
	{
		this.options = options;
	}

	@SuppressWarnings("unchecked")
	public <T> T getOption(String name)
	{
		return (T) (this.options == null ? null : this.options.get(name));
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [options=" + options + "]";
	}
}
