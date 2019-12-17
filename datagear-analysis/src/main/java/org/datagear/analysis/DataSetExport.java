/*
 * Copyright (c) 2018 datagear.tech. All Rights Reserved.
 */

/**
 * 
 */
package org.datagear.analysis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据集输出项。
 * <p>
 * 此类描述{@linkplain DataSetFactory}创建的{@linkplain DataSet}可输出的数据信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public abstract class DataSetExport implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 名称 */
	private String name;

	/** 类型 */
	private DataType type;

	public DataSetExport()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public DataType getType()
	{
		return type;
	}

	public void setType(DataType type)
	{
		this.type = type;
	}

	/**
	 * 获取输出项值。
	 * 
	 * @param meta
	 * @param datas
	 * @return
	 * @throws DataSetException
	 */
	public abstract Object getExportValue(DataSetMeta meta, List<Map<String, ?>> datas) throws DataSetException;

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + name + ", type=" + type + "]";
	}
}
