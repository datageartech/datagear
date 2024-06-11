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

import java.util.List;

/**
 * 数据集。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSet extends Identifiable
{
	/**
	 * 获取名称。
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 是否是易变模型。
	 * <p>
	 * {@linkplain #getResult(DataSetQuery)}返回数据的结构并不是固定不变，也不是可以由{@linkplain #getFields()}描述的。
	 * </p>
	 * 
	 * @return
	 */
	boolean isMutableModel();

	/**
	 * 获取{@linkplain DataSetField}列表。
	 * 
	 * @return 返回空列表则表示没有
	 */
	List<DataSetField> getFields();

	/**
	 * 获取指定名称的{@linkplain DataSetField}，没有则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSetField getField(String name);

	/**
	 * 获取参数列表。
	 * 
	 * @return 参数列表，返回空列表则表示无参数
	 */
	List<DataSetParam> getParams();

	/**
	 * 获取指定名称的参数，没有则返回{@code null}。
	 * 
	 * @param name
	 * @return
	 */
	DataSetParam getParam(String name);

	/**
	 * 获取{@linkplain DataSetResult}。
	 * <p>
	 * 如果{@linkplain #isMutableModel()}为{@code false}，那么返回结果中的数据项字段不应超出{@linkplain #getFields()}的范围，
	 * 避免暴露底层数据源不期望暴露的数据；
	 * 如果{@linkplain #isMutableModel()}为{@code true}，则返回结果中的数据项字段不受{@linkplain #getFields()}范围限制。
	 * </p>
	 * <p>
	 * 如果返回结果中的数据项字段在{@linkplain #getFields()}中有对应，当数据项字段值为{@code null}时，应使用{@linkplain DataSetField#getDefaultValue()}的值。
	 * </p>
	 * <p>
	 * 返回结果中的数据项字段值应已转换为与{@linkplain #getFields()}的{@linkplain DataSetField#getType()}类型一致。
	 * </p>
	 * 
	 * @param query
	 * @return
	 * @throws DataSetException
	 */
	DataSetResult getResult(DataSetQuery query) throws DataSetException;
}
