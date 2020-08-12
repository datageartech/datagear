/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import java.util.List;
import java.util.Map;

import org.datagear.analysis.DataSetException;
import org.datagear.analysis.DataSetProperty;
import org.datagear.analysis.DataSetResult;

/**
 * 抽象JSON源数据集。
 * <p>
 * JSON源数据集的一个特点是：源数据是无法编辑的，因此需要定义{@linkplain DataSetResultTransformer}逻辑。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class AbstractJsonSourceDataSet extends AbstractJsonDataSet
{
	private DataSetResultTransformer dataSetResultTransformer;

	public AbstractJsonSourceDataSet()
	{
		super();
	}

	public AbstractJsonSourceDataSet(String id, String name, List<DataSetProperty> properties)
	{
		super(id, name, properties);
	}

	/**
	 * 获取{@linkplain DataSetResultTransformer}。
	 * 
	 * @return 可能为{@code null}
	 */
	public DataSetResultTransformer getDataSetResultTransformer()
	{
		return dataSetResultTransformer;
	}

	public void setDataSetResultTransformer(DataSetResultTransformer dataSetResultTransformer)
	{
		this.dataSetResultTransformer = dataSetResultTransformer;
	}

	@Override
	public DataSetResult getResult(Map<String, ?> paramValues) throws DataSetException
	{
		DataSetResult result = getOrginalResult(paramValues);

		if (this.dataSetResultTransformer == null)
			return result;

		return this.dataSetResultTransformer.transform(result);
	}

	/**
	 * 获取原始的{@linkplain DataSetResult}。
	 * 
	 * @param paramValues
	 * @return
	 * @throws DataSetException
	 */
	protected abstract DataSetResult getOrginalResult(Map<String, ?> paramValues) throws DataSetException;
}
