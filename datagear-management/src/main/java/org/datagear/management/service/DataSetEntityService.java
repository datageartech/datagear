/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.support.SqlDataSet;
import org.datagear.management.domain.DataSetEntity;

/**
 * {@linkplain DataSetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetEntityService extends DataPermissionEntityService<String, DataSetEntity>
{
	/**
	 * 获取可用于执行分析的{@linkplain SqlDataSet}。
	 * 
	 * @param id
	 * @return
	 */
	DataSet getDataSet(String id);
}
