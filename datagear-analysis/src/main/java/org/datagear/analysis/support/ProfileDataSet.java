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

package org.datagear.analysis.support;

import java.io.Serializable;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetQuery;
import org.datagear.analysis.DataSetResult;

/**
 * 轮廓{@linkplain DataSet}。
 * <p>
 * 此类仅用于描述{@linkplain DataSet}接口数据结构，不包含任何其他逻辑。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class ProfileDataSet extends AbstractDataSet implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ProfileDataSet()
	{
	}

	public ProfileDataSet(DataSet dataSet)
	{
		super(dataSet.getId(), dataSet.getName(), dataSet.getProperties());
		setMutableModel(dataSet.isMutableModel());
		setParams(dataSet.getParams());
	}

	@Override
	public DataSetResult getResult(DataSetQuery query) throws DataSetException
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * 构建{@linkplain ProfileDataSet}。
	 * <p>
	 * 如果{@code dataSet}是{@linkplain ProfileDataSet}实例，将直接返回。
	 * </p>
	 * 
	 * @param dataSet
	 *            允许为{@code null}
	 * @return
	 */
	public static ProfileDataSet valueOf(DataSet dataSet)
	{
		if (dataSet == null)
			return null;

		if (dataSet instanceof ProfileDataSet)
			return (ProfileDataSet) dataSet;

		return new ProfileDataSet(dataSet);
	}
}
