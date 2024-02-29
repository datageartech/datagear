/*
 * Copyright 2018-2024 datagear.tech
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

import java.util.Map;

/**
 * 可解析{@linkplain DataSetResult}。
 * <p>
 * 调用{@linkplain #resolve(Map)}无需预先设置{@linkplain #getProperties()}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface ResolvableDataSet extends DataSet
{
	/**
	 * 解析{@linkplain ResolvedDataSetResult}。
	 * <p>
	 * 返回的{@linkplain ResolvedDataSetResult#getProperties()}是从数据中解析的信息。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws DataSetException
	 */
	ResolvedDataSetResult resolve(DataSetQuery query) throws DataSetException;
}
