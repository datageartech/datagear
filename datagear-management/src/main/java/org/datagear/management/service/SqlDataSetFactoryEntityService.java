/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import org.datagear.analysis.support.SqlDataSetFactory;
import org.datagear.management.domain.SqlDataSetFactoryEntity;

/**
 * {@linkplain SqlDataSetFactoryEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface SqlDataSetFactoryEntityService extends DataPermissionEntityService<String, SqlDataSetFactoryEntity>
{
	/**
	 * 获取可用于执行分析的{@linkplain SqlDataSetFactory}。
	 * 
	 * @param id
	 * @return
	 */
	SqlDataSetFactory getSqlDataSetFactory(String id);
}
