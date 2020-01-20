/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import org.datagear.analysis.support.SqlDataSet;
import org.datagear.management.domain.SqlDataSetEntity;

/**
 * {@linkplain SqlDataSetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SqlDataSetEntityService extends DataPermissionEntityService<String, SqlDataSetEntity>
{
	/**
	 * 获取可用于执行分析的{@linkplain SqlDataSet}。
	 * 
	 * @param id
	 * @return
	 */
	SqlDataSet getSqlDataSet(String id);
}
