/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis.support;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetResult;

/**
 * {@linkplain DataSetResult}转换器。
 * <p>
 * 某些类型的{@linkplain DataSet}是从不可控制的数据源中读取数据的（比如API调用、JSON文件、CSV文件），
 * 此类即为这些场景提供支持，使{@linkplain DataSet}支持对数据源的数据进行转换。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetResultTransformer
{
	/**
	 * 转换为新的{@linkplain DataSetResult}。
	 * 
	 * @param orginalResult
	 * @return 已转换的{@linkplain DataSetResult}
	 * @throws DataSetResultTransformException
	 */
	DataSetResult transform(DataSetResult orginalResult) throws DataSetResultTransformException;
}
