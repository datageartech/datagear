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

import org.datagear.util.StringUtil;

/**
 * 数据集属性信息。
 * <p>
 * 此类描述{@linkplain DataSet#getResult(Map)}返回的{@linkplain DataSetResult#getDatas()}元素的属性信息。
 * </p>
 * 
 * @author datagear@163.com
 *
 */
public class DataSetProperty extends AbstractDataNameType implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** 展示标签 */
	private String label;

	public DataSetProperty()
	{
		super();
	}

	public DataSetProperty(String name, DataType type)
	{
		super(name, type);
	}

	public boolean hasLabel()
	{
		return (this.label != null && !this.label.isEmpty());
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + ", type=" + getType() + ", label=" + label + "]";
	}

	/**
	 * 连接给定列表的{@linkplain #getLabel()}。
	 * <p>
	 * 如果{@code dataSetProperties}为{@code null}，将返回空字符串。
	 * </p>
	 * 
	 * @param dataSetProperties
	 * @param splitter
	 */
	public static String concatLabels(List<DataSetProperty> dataSetProperties, String splitter)
	{
		if (dataSetProperties == null)
			return "";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < dataSetProperties.size(); i++)
		{
			DataSetProperty dataSetProperty = dataSetProperties.get(i);

			String label = dataSetProperty.getLabel();
			if (!StringUtil.isEmpty(label))
			{
				if (sb.length() > 0)
					sb.append(splitter);

				sb.append(label);
			}
		}

		return sb.toString();
	}

	/**
	 * 拆分由{@linkplain #concatLabels(List, String)}连接的字符串。
	 * 
	 * @param labelText
	 * @param splitter
	 * @return
	 */
	public static String[] splitLabels(String labelText, String splitter)
	{
		if (labelText == null)
			return new String[0];

		return StringUtil.split(labelText, splitter, true);
	}
}
