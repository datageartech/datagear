/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.management.service;

import java.io.File;

import org.datagear.analysis.DataSet;
import org.datagear.management.domain.DataSetEntity;
import org.datagear.management.domain.JsonFileDataSetEntity;

/**
 * {@linkplain DataSetEntity}业务服务接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetEntityService extends DataPermissionEntityService<String, DataSetEntity>
{
	/**
	 * 获取可用于执行分析的{@linkplain DataSet}。
	 * 
	 * @param id
	 * @return
	 */
	DataSet getDataSet(String id);

	/**
	 * 获取{@linkplain JsonFileDataSetEntity#getDirectory()}。
	 * 
	 * @return
	 */
	File getDataSetJsonFileDirectory();

	/**
	 * 设置{@linkplain JsonFileDataSetEntity}，使其具备执行分析的条件。
	 * 
	 * @param entity
	 */
	void inflateJsonFileDataSetEntity(JsonFileDataSetEntity entity);
}
