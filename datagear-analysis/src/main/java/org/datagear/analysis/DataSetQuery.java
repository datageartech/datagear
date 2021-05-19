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
 * 此类用于从{@linkplain DataSet}中查询结果数据。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetQuery
{
	/** 参数值映射表 */
	private Map<String, Object> paramValues = Collections.emptyMap();

	/**结果数据格式*/
	private ResultDataFormat resultDataFormat = null;
	
	public DataSetQuery()
	{
		super();
	}

	public Map<String, Object> getParamValues()
	{
		return paramValues;
	}

	public void setParamValues(Map<String, Object> paramValues)
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
}
