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

import java.io.Serializable;

/**
 * 数据集结果的JSON规则。
 * <p>
 * 用于设置从数据集原始数据中解析结果数据、结果附加数据的规则。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ResultJsonRule implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * 结果数据在原始数据中的JSON路径，比如：
	 * <p>
	 * <code>"stores[0].books"</code>、 <code>"[1].stores"</code>、
	 * <code>"$['store']['book'][0]"</code>、
	 * <code>"$.store.book[*].author"</code>、 <code>"$..book[2]"</code>
	 * </p>
	 */
	private String dataJsonPath = null;

	/**
	 * 结果附加数据在原始数据中的JSON路径配置，比如：
	 * <p>
	 * <code>"{name:'label', value:'$.stores[0].books'}"</code>
	 * </p>
	 * <p>
	 * 表示将原始数据中的{@code "label"}对应的值设置为{@code "name"}标识的附加数据，将{@code "$.stores[0].books"}对应的值设置为{@code "value"}标识的附加数据
	 * </p>
	 */
	private String additionJsonPath = null;

	public ResultJsonRule()
	{
		super();
	}

	public ResultJsonRule(String dataJsonPath)
	{
		super();
		this.dataJsonPath = dataJsonPath;
	}

	public ResultJsonRule(String dataJsonPath, String additionJsonPath)
	{
		super();
		this.dataJsonPath = dataJsonPath;
		this.additionJsonPath = additionJsonPath;
	}

	public String getDataJsonPath()
	{
		return dataJsonPath;
	}

	public void setDataJsonPath(String dataJsonPath)
	{
		this.dataJsonPath = dataJsonPath;
	}

	public String getAdditionJsonPath()
	{
		return additionJsonPath;
	}

	public void setAdditionJsonPath(String additionJsonPath)
	{
		this.additionJsonPath = additionJsonPath;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((additionJsonPath == null) ? 0 : additionJsonPath.hashCode());
		result = prime * result + ((dataJsonPath == null) ? 0 : dataJsonPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultJsonRule other = (ResultJsonRule) obj;
		if (additionJsonPath == null)
		{
			if (other.additionJsonPath != null)
				return false;
		}
		else if (!additionJsonPath.equals(other.additionJsonPath))
			return false;
		if (dataJsonPath == null)
		{
			if (other.dataJsonPath != null)
				return false;
		}
		else if (!dataJsonPath.equals(other.dataJsonPath))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [dataJsonPath=" + dataJsonPath + ", additionJsonPath=" + additionJsonPath
				+ "]";
	}
}