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
	 * 
	 * @param query 应是已通过{@linkplain #isReady(DataSetQuery)}校验的（可能为{@code null}）
	 * @return
	 * @throws DataSetException
	 */
	ResolvedDataSetResult resolve(DataSetQuery query) throws DataSetException;
}
