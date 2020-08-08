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

	String PROPERTY_LABELS_SPLITTER = ",";

	/** 数据集类型：SQL */
	String DATA_SET_TYPE_SQL = "SQL";

	/** 数据集类型：JSON值 */
	String DATA_SET_TYPE_JSON_VALUE = "JSON_VALUE";

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

	/**
	 * 获取属性集的标签文本表示形式。
	 * 
	 * @return
	 */
	String getPropertyLabelsText();

	/**
	 * 设置属性集的标签文本表示形式。
	 * 
	 * @param text
	 */
	void setPropertyLabelsText(String text);

	/**
	 * 获取属性集的标签文本表示形式。
	 * 
	 * @param properties
	 * @return
	 */
	public static String getPropertyLabelsText(List<DataSetProperty> properties)
	{
		return DataSetProperty.concatLabels(properties, PROPERTY_LABELS_SPLITTER);
	}

	/**
	 * 设置属性集的标签文本表示形式。
	 * 
	 * @param properties
	 * @param text
	 */
	public static void setPropertyLabelsText(List<DataSetProperty> properties, String text)
	{
		String[] labels = DataSetProperty.splitLabels(text, PROPERTY_LABELS_SPLITTER);

		if (labels == null || labels.length == 0)
			return;

		for (int i = 0; i < Math.min(labels.length, properties.size()); i++)
		{
			if (i < labels.length)
				properties.get(i).setLabel(labels[i]);
		}
	}
}
