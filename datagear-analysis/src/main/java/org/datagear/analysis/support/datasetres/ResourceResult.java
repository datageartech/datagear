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

package org.datagear.analysis.support.datasetres;

import java.io.Serializable;
import java.util.Collection;

import org.datagear.analysis.DataSetResult;
import org.datagear.analysis.ResolvedDataSetResult;

/**
 * 从{@linkplain DataSetResource}解析而得的结果。
 * 
 * @author datagear@163.com
 *
 */
public class ResourceResult implements Serializable
{
	private static final long serialVersionUID = 1L;

	private ResolvedDataSetResult result;

	public ResourceResult()
	{
		super();
	}

	public ResourceResult(ResolvedDataSetResult result)
	{
		super();
		this.result = result;
	}

	/**
	 * 获取结果。
	 * <p>
	 * 注意：返回的结果及其内容不应被修改，因为可能会缓存。
	 * </p>
	 * 
	 * @return
	 */
	public ResolvedDataSetResult getResult()
	{
		return result;
	}

	public void setResult(ResolvedDataSetResult result)
	{
		this.result = result;
	}

	/**
	 * 获取当{@linkplain #getResult()}的{@linkplain ResolvedDataSetResult#getResult()}的{@linkplain DataSetResult#getData()}
	 * 是数组、集合时的长度。
	 * 
	 * @return {@code -1}表示不是数组、集合
	 */
	public int dataSize()
	{
		DataSetResult dr = (this.result == null ? null : this.result.getResult());
		Object data = (dr == null ? null : dr.getData());

		if (data == null)
			return -1;

		if (data instanceof Collection<?>)
			return ((Collection<?>) data).size();

		if (data instanceof Object[])
			return ((Object[]) data).length;

		return -1;
	}
}
