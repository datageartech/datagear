/*
 * Copyright 2018 datagear.tech. All Rights Reserved.
 */

package org.datagear.management.domain;

import java.util.Date;
import java.util.List;

import org.datagear.analysis.DataSet;
import org.datagear.analysis.DataSetParam;
import org.datagear.analysis.DataSetProperty;

/**
 * 数据集实体接口。
 * 
 * @author datagear@163.com
 *
 */
public interface DataSetEntity extends DataSet, CreateUserEntity<String>, DataPermissionEntity<String>
{
	/** 授权资源类型 */
	String AUTHORIZATION_RESOURCE_TYPE = "DataSet";

	/** 数据集类型：SQL */
	String DATA_SET_TYPE_SQL = "SQL";

	/** 数据集类型：JSON值 */
	String DATA_SET_TYPE_JsonValue = "JsonValue";

	/** 数据集类型：JSON文件 */
	String DATA_SET_TYPE_JsonFile = "JsonFile";

	/** 数据集类型：Excel */
	String DATA_SET_TYPE_Excel = "Excel";

	/**
	 * 设置名称。
	 * 
	 * @param name
	 */
	void setName(String name);

	/**
	 * 设置属性集。
	 * 
	 * @param properties
	 */
	void setProperties(List<DataSetProperty> properties);

	/**
	 * 设置参数集。
	 * 
	 * @param params
	 */
	void setParams(List<DataSetParam> params);

	/**
	 * 获取数据集类型。
	 * 
	 * @return
	 */
	String getDataSetType();

	/**
	 * 设置数据集类型。
	 * 
	 * @param dataSetType
	 */
	void setDataSetType(String dataSetType);

	/**
	 * 获取创建时间。
	 * 
	 * @return
	 */
	Date getCreateTime();

	/**
	 * 设置创建时间。
	 * 
	 * @param createTime
	 */
	void setCreateTime(Date createTime);
}
