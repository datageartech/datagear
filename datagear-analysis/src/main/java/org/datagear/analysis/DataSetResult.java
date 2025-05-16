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

package org.datagear.analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据集结果。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetResult implements AdditionsAware, Serializable
{
	private static final long serialVersionUID = 1L;

	/** 结果数据对象 */
	private Object data = null;

	/**
	 * 附加数据集合。
	 * <p>
	 * {@linkplain DataSet}的数据源中除了结果数据，可能还有附加数据，应写入此映射表中。
	 * </p>
	 */
	private Map<String, ?> additions = null;

	public DataSetResult()
	{
		super();
	}

	/**
	 * 创建实例。
	 * 
	 * @param data
	 *            应符合{@linkplain #getData()}规范
	 */
	public DataSetResult(Object data)
	{
		super();
		this.data = data;
	}

	/**
	 * 获取数据。
	 * <p>
	 * 数据应是普通JavaBean、 {@linkplain Map}对象，或者是它们的数组、集合。
	 * </p>
	 * 
	 * @return 数据，为{@code null}表示无数据
	 */
	public Object getData()
	{
		return this.data;
	}

	/**
	 * 设置数据。
	 * 
	 * @param data
	 *            应符合{@linkplain #getData()}规范
	 */
	public void setData(Object data)
	{
		this.data = data;
	}

	@Override
	public Map<String, ?> getAdditions()
	{
		return additions;
	}

	public void setAdditions(Map<String, ?> additions)
	{
		this.additions = additions;
	}

	/**
	 * 添加附加数据。
	 * <p>
	 * 如果{@linkplain #getAdditions()}为{@code null}，它会默认创建{@linkplain HashMap}。
	 * </p>
	 * 
	 * @param name
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void addAddition(String name, Object value)
	{
		if (this.additions == null)
			this.additions = new HashMap<>();

		((Map<String, Object>) this.additions).put(name, value);
	}

	/**
	 * 添加附加数据。
	 * <p>
	 * 如果{@linkplain #getAdditions()}为{@code null}，它会默认创建{@linkplain HashMap}。
	 * </p>
	 * 
	 * @param name
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void addAdditions(Map<String, ?> additions)
	{
		if (this.additions == null)
			this.additions = new HashMap<>();

		((Map<String, Object>) this.additions).putAll(additions);
	}

	/**
	 * 获取附加数据。
	 * 
	 * @param <T>
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAddition(String name)
	{
		if (this.additions == null)
			return null;
		else
			return (T) this.additions.get(name);
	}

	/**
	 * 移除并返回附加数据。
	 * 
	 * @param <T>
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T removeAddition(String name)
	{
		if (this.additions == null)
			return null;
		else
			return (T) this.additions.remove(name);
	}
}
