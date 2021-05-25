/*
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 */

package org.datagear.analysis;

import java.util.Collections;
import java.util.Map;

/**
 * 数据集查询。
 * <p>
 * 此类用于从{@linkplain DataSet}中查询{@linkplain DataSetResult}。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetQuery
{
	/** 参数值映射表 */
	private Map<String, ?> paramValues = Collections.emptyMap();

	/**结果数据格式*/
	private ResultDataFormat resultDataFormat = null;

	/** 结果数据最大返回数目 */
	private int resultFetchSize = -1;

	public DataSetQuery()
	{
		super();
	}

	public DataSetQuery(DataSetQuery query)
	{
		super();
		this.paramValues = query.paramValues;
		this.resultDataFormat = query.resultDataFormat;
		this.resultFetchSize = query.resultFetchSize;
	}

	public Map<String, ?> getParamValues()
	{
		return paramValues;
	}

	/**
	 * 设置参数值映射表。
	 * <p>
	 * 参数值映射表的关键字是{@linkplain DataSet#getParam(String)}中的{@linkplain DataSetParam#getName()}，应是符合{@linkplain DataSet#isReady(DataSetQuery)}校验的，
	 * 参数值映射表并不要求与{@linkplain #getParams()}一一对应，通常是包含相同、或者更多的项。
	 * </p>
	 * 
	 * @param paramValues
	 */
	public void setParamValues(Map<String, ?> paramValues)
	{
		this.paramValues = paramValues;
	}
	
	/**
	 * 获取结果数据格式。
	 * 
	 * @return 返回{@code null}表示未设置
	 */
	public ResultDataFormat getResultDataFormat()
	{
		return resultDataFormat;
	}

	/**
	 * 设置结果数据格式。
	 * <p>
	 * 当希望自定义{@linkplain DataSet#getResult(Map)}的{@linkplain DataSetResult#getData()}数据格式时，可以设置此项。
	 * </p>
	 * 
	 * @param dataFormat
	 */
	public void setResultDataFormat(ResultDataFormat resultDataFormat)
	{
		this.resultDataFormat = resultDataFormat;
	}
	
	/**
	 * 获取结果数据最大返回数目。
	 * 
	 * @return {@code <0} 表示不限定数目
	 */
	public int getResultFetchSize()
	{
		return resultFetchSize;
	}

	public void setResultFetchSize(int resultFetchSize)
	{
		this.resultFetchSize = resultFetchSize;
	}

	/**
	 * 浅复制此对象。
	 * 
	 * @return
	 */
	public DataSetQuery copy()
	{
		return new DataSetQuery(this);
	}

	/**
	 * 构建{@linkplain DataSetQuery}。
	 * 
	 * @return
	 */
	public static DataSetQuery valueOf()
	{
		return new DataSetQuery();
	}
	
	/**
	 * 构建{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @return
	 */
	public static DataSetQuery valueOf(Map<String, ?> paramValues)
	{
		DataSetQuery query = new DataSetQuery();
		query.setParamValues(paramValues);
		
		return query;
	}
	
	/**
	 * 构建{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param resultDataFormat
	 * @return
	 */
	public static DataSetQuery valueOf(Map<String, ?> paramValues, ResultDataFormat resultDataFormat)
	{
		DataSetQuery query = valueOf(paramValues);
		query.setResultDataFormat(resultDataFormat);
		
		return query;
	}
	
	/**
	 * 构建{@linkplain DataSetQuery}。
	 * 
	 * @param paramValues
	 * @param resultDataFormat
	 * @param resultFetchSize
	 * @return
	 */
	public static DataSetQuery valueOf(Map<String, ?> paramValues, ResultDataFormat resultDataFormat, int resultFetchSize)
	{
		DataSetQuery query = valueOf(paramValues, resultDataFormat);
		query.setResultFetchSize(resultFetchSize);
		
		return query;
	}
	
	/**
	 * 构建{@linkplain DataSetQuery}。
	 * 
	 * @param query
	 * @return
	 */
	public static DataSetQuery valueOf(DataSetQuery query)
	{
		return new DataSetQuery(query);
	}

	/**
	 * 拷贝。
	 * 
	 * @param query 允许为{@code null}
	 * @return 如果{@code query}为{@code null}，将返回{@code new DataSetQuery()}。
	 */
	public static DataSetQuery copy(DataSetQuery query)
	{
		if(query == null)
			return DataSetQuery.valueOf();
		
		return query.copy();
	}
}
