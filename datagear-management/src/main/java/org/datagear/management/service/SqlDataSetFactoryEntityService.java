/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import java.util.List;

import org.datagear.management.domain.HtmlChartWidgetEntity;
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
	 * 查找{@linkplain HtmlChartWidgetEntity#getId()}关联的所有{@linkplain SqlDataSetFactoryEntity}。
	 * 
	 * @param id
	 * @return
	 */
	List<SqlDataSetFactoryEntity> findByHtmlChartWidgetEntityId(String id);
}
