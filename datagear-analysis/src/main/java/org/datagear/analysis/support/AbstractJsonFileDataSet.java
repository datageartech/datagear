/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;

/**
 * 抽象JSON文件数据集。
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonFileDataSet extends AbstractJsonDataSet
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	/** 文件编码 */
	private String encoding = DEFAULT_ENCODING;

	public AbstractJsonFileDataSet()
	{
		super();
	}

	public AbstractJsonFileDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}
	
	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		File jsonFile = getJsonFile(paramValues);
		Object data = getJsonDataSetSupport().resolveResultData(jsonFile, getEncoding());
		return new DataSetResult(data);
	}

	/**
	 * 获取JSON文件。
	 * 
	 * @param paramValues
	 * @return
	 * @throws DataSetException
	 */
	protected abstract File getJsonFile(Map<String, ?> paramValues) throws DataSetException;
}
