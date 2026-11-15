/*
 * Copyright 2018-2024 datagear.tech
 */

package org.datagear.management.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 防护属性。
 * 
 * @author datagear@163.com
 *
 */
public class GuardProperty implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	private String name;

	private Object value;

	public GuardProperty()
	{
		super();
	}

	public GuardProperty(String name, Object value)
	{
		super();
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public GuardProperty clone()
	{
		return new GuardProperty(this.name, this.value);
	}

	/**
	 * 转换为{@linkplain GuardProperty}集合。
	 * 
	 * @param <T>
	 * @param sources
	 * @param converter
	 * @return
	 */
	public static <T> List<GuardProperty> convert(List<T> sources, Function<T, GuardProperty> converter)
	{
		if (sources == null)
			return null;

		if (sources.isEmpty())
			return Collections.emptyList();

		List<GuardProperty> re = new ArrayList<>(sources.size());

		for (T s : sources)
		{
			GuardProperty gp = converter.apply(s);
			re.add(gp);
		}

		return re;
	}

	/**
	 * 转换为{@linkplain GuardProperty}集合。
	 * 
	 * @param <T>
	 * @param sources
	 * @return
	 */
	public static <T> List<GuardProperty> convert(Map<String, ?> sources)
	{
		if (sources == null)
			return null;

		if (sources.isEmpty())
			return Collections.emptyList();

		List<GuardProperty> re = new ArrayList<>(sources.size());

		for (Map.Entry<String, ?> entry : sources.entrySet())
		{
			GuardProperty gp = new GuardProperty(entry.getKey(), entry.getValue());
			re.add(gp);
		}

		return re;
	}

	/**
	 * 复制。
	 * 
	 * @param src
	 *            允许{@code null}
	 * @return
	 */
	public static List<GuardProperty> clones(Collection<GuardProperty> src)
	{
		if (src == null)
			return null;

		if (src.isEmpty())
			return Collections.emptyList();

		List<GuardProperty> re = new ArrayList<>(src.size());

		for (GuardProperty s : src)
		{
			re.add(s == null ? null : s.clone());
		}

		return re;
	}
}