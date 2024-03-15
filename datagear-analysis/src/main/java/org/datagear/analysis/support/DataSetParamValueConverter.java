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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.NameTypeAware;
import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetParam.DataType;
import org.datagear.analysis.DataSetQuery;

/**
 * {@linkplain DataSetParam}值转换器。
 * 
 * @author datagear@163.com
 *
 */
public class DataSetParamValueConverter extends DataValueConverter
{
	public DataSetParamValueConverter()
	{
		super();
	}
	
	/**
	 * 将{@linkplain DataSetQuery#getParamValues()}转换为匹配{@linkplain DataSet#getParams()}类型的映射表，
	 * 并返回一个新的{@linkplain DataSetQuery}。
	 * <p>
	 * 如果{@linkplain DataSetQuery#getParamValues()}中有未在{@linkplain DataSet#getParams()}中定义的项，
	 * 那么它将原样写入返回的{@linkplain DataSetQuery}。
	 * </p>
	 * <p>
	 * 因为对于支持<code>Freemarker</code>的{@linkplain DataSet}实现类（比如：{@linkplain SqlDataSet}），
	 * 存在不定义{@linkplain DataSet#getParams()}而传递参数给内部<code>Freemarker</code>模板的应用场景，
	 * 也会存在传递系统上下文变量的场景（比如传递系统当前用户）。
	 * </p>
	 * 
	 * @param query
	 * @param dataSet
	 * @return 当{@code query}为{@code null}时 ，将返回{@code null}
	 */
	public DataSetQuery convert(DataSetQuery query, DataSet dataSet)
	{
		return convert(query, dataSet, false);
	}
	
	/**
	 * 将{@linkplain DataSetQuery#getParamValues()}转换为匹配{@linkplain DataSet#getParams()}类型的映射表，
	 * 并返回一个新的{@linkplain DataSetQuery}。
	 * <p>
	 * 如果{@linkplain DataSetQuery#getParamValues()}中有未在{@linkplain DataSet#getParams()}中定义的项，
	 * 那么它将原样写入返回的{@linkplain DataSetQuery}。
	 * </p>
	 * <p>
	 * 因为对于支持<code>Freemarker</code>的{@linkplain DataSet}实现类（比如：{@linkplain SqlDataSet}），
	 * 存在不定义{@linkplain DataSet#getParams()}而传递参数给内部<code>Freemarker</code>模板的应用场景，
	 * 也会存在传递系统上下文变量的场景（比如传递系统当前用户）。
	 * </p>
	 * 
	 * @param query         允许为{@code null}
	 * @param dataSet
	 * @param returnNonNull
	 * @return 当{@code query}为{@code null}且{@code returnNonNull}为{@code false}时
	 *         ，将返回{@code null}
	 */
	public DataSetQuery convert(DataSetQuery query, DataSet dataSet, boolean returnNonNull)
	{
		if(query == null)
		{
			return (returnNonNull ? DataSetQuery.valueOf() : null);
		}
		
		DataSetQuery reQuery = query.copy();
		
		Map<String, ?> reParamValues = reQuery.getParamValues();
		List<DataSetParam> dataSetParams = dataSet.getParams();
		
		reParamValues = convert(reParamValues, dataSetParams);
		reQuery.setParamValues(reParamValues);
		
		return reQuery;
	}

	/**
	 * 转换参数值映射表，返回一个经转换的新映射表。
	 * <p>
	 * 如果{@code paramValues}中有未在{@code dataSetParams}中定义的项，那么它将原样写入返回映射表中。
	 * </p>
	 * <p>
	 * 因为对于支持<code>Freemarker</code>的{@linkplain DataSet}实现类（比如：{@linkplain SqlDataSet}），
	 * 存在不定义{@linkplain DataSet#getParams()}而传递参数给内部<code>Freemarker</code>模板的应用场景，
	 * 也会存在传递系统上下文变量的场景（比如传递系统当前用户）。
	 * </p>
	 */
	@Override
	public Map<String, Object> convert(Map<String, ?> paramValues, Collection<? extends NameTypeAware> dataSetParams)
			throws DataValueConvertionException
	{
		return super.convert(paramValues, dataSetParams);
	}

	@Override
	protected Object convertValue(Object value, String type) throws DataValueConvertionException
	{
		if (DataType.STRING.equals(type))
			return convertToString(value, DataType.STRING);
		else if (DataType.NUMBER.equals(type))
			return convertToNumber(value, DataType.NUMBER);
		else if (DataType.BOOLEAN.equals(type))
			return convertToBoolean(value, DataType.BOOLEAN);
		else
			return convertExt(value, type);
	}
}
