/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis.support;

import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
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
public class ProfileDataSet extends AbstractDataSet
{
	private static final long serialVersionUID = 1L;

	public ProfileDataSet()
	{
	}

	public ProfileDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	public ProfileDataSet(DataSet dataSet)
	{
		super(dataSet.getId(), dataSet.getName(), dataSet.getProperties());
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
